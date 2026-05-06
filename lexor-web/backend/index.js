const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const app = express();
const port = 5000;

app.use(cors());
app.use(bodyParser.json());

app.post('/api/compile-and-run', (req, res) => {
    const { code, inputs } = req.body;
    
    if (!code) {
        return res.status(400).json({ error: 'No code provided' });
    }

    // Path to program.lexor in the project root
    const lexorFilePath = path.join(__dirname, '../../program.lexor');
    
    // Write code to file
    try {
        fs.writeFileSync(lexorFilePath, code);
    } catch (err) {
        return res.status(500).json({ error: 'Failed to write code to file: ' + err.message });
    }

    // Execute Java process
    // We assume the classes are in ../../target/classes
    const classpath = path.join(__dirname, '../../target/classes');
    const projectRoot = path.join(__dirname, '../../');
    const javaProcess = spawn('java', ['-cp', classpath, 'com.lexor.core.Main'], {
        cwd: projectRoot
    });

    let output = '';
    let error = '';

    javaProcess.stdout.on('data', (data) => {
        output += data.toString();
    });

    javaProcess.stderr.on('data', (data) => {
        error += data.toString();
    });

    // Provide inputs to stdin
    if (inputs) {
        javaProcess.stdin.write(inputs + '\n');
    }
    javaProcess.stdin.end();

    javaProcess.on('close', (code) => {
        res.json({
            output: output,
            error: error,
            exitCode: code
        });
    });

    javaProcess.on('error', (err) => {
        res.status(500).json({ error: 'Failed to start Java process: ' + err.message });
    });
});

app.listen(port, () => {
    console.log(`Lexor Backend Bridge running at http://localhost:${port}`);
});
