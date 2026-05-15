const express = require("express");
const cors = require("cors");
const http = require("http");
const { Server } = require("socket.io");
const { spawn } = require("child_process");
const fs = require("fs");
const path = require("path");

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
	cors: {
		origin: "http://localhost:5173",
		methods: ["GET", "POST"],
	},
});

const port = 5000;

app.use(cors());
app.use(express.json());

io.on("connection", (socket) => {
	console.log("Client connected:", socket.id);

	socket.on("run-code", ({ code }) => {
		if (!code) {
			socket.emit("error", "No code provided");
			return;
		}

		const lexorFilePath = path.join(__dirname, "../../program.lexor");
		try {
			fs.writeFileSync(lexorFilePath, code);
		} catch (err) {
			socket.emit("error", "Failed to write code to file: " + err.message);
			return;
		}

		const classpath = path.join(__dirname, "../../target/classes");
		const projectRoot = path.join(__dirname, "../../");
		const javaProcess = spawn("java", ["-cp", classpath, "com.lexor.core.Main"], {
			cwd: projectRoot,
		});

		socket.on("terminal-input", (input) => {
			if (javaProcess && !javaProcess.killed) {
				javaProcess.stdin.write(input + "\n");
			}
		});

		javaProcess.stdout.on("data", (data) => {
			socket.emit("output", data.toString());
		});

		javaProcess.stderr.on("data", (data) => {
			socket.emit("output", data.toString()); // Send stderr as output too
		});

		javaProcess.on("close", (code) => {
			socket.emit("exit", code);
		});

		javaProcess.on("error", (err) => {
			socket.emit("error", "Failed to start Java process: " + err.message);
		});

		socket.on("disconnect", () => {
			if (javaProcess && !javaProcess.killed) {
				javaProcess.kill();
			}
		});
	});
});

server.listen(port, () => {
	console.log(`Lexor Backend Bridge running at http://localhost:${port}`);
});
