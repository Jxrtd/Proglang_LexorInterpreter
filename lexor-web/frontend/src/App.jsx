import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import Editor from '@monaco-editor/react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Play, 
  Terminal, 
  Cpu, 
  Settings, 
  Copy, 
  Trash2, 
  ChevronRight, 
  Command,
  Zap,
  CheckCircle2,
  AlertCircle,
  FileCode
} from 'lucide-react';
import { cn } from './lib/utils';

// --- Professional UI Components ---

const Panel = ({ children, className, title, icon: Icon }) => (
  <motion.div 
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    className={cn("relative flex flex-col rounded-2xl border border-white/5 bg-black/40 backdrop-blur-xl overflow-hidden shadow-2xl", className)}
  >
    <div className="flex items-center justify-between px-4 py-3 border-b border-white/5 bg-white/5">
      <div className="flex items-center gap-2">
        {Icon && <Icon size={14} className="text-blue-400" />}
        <span className="text-[10px] uppercase tracking-[0.2em] font-bold text-gray-400">{title}</span>
      </div>
      <div className="flex gap-1.5">
        <div className="w-2 h-2 rounded-full bg-red-500/20 border border-red-500/40" />
        <div className="w-2 h-2 rounded-full bg-yellow-500/20 border border-yellow-500/40" />
        <div className="w-2 h-2 rounded-full bg-green-500/20 border border-green-500/40" />
      </div>
    </div>
    {children}
  </motion.div>
);

const App = () => {
  const [code, setCode] = useState(`START SCRIPT
DECLARE name
PRINT: "--- PROFESSIONAL LEXOR IDE ---"
PRINT: "Initializing System..."
PRINT: "Enter Operator Name: "
SCAN name
PRINT: "Access Granted: Welcome, " & name
END SCRIPT`);
  const [inputs, setInputs] = useState('Lexor User');
  const [output, setOutput] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('READY');
  const outputRef = useRef(null);

  // Auto-scroll terminal
  useEffect(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop = outputRef.current.scrollHeight;
    }
  }, [output, error]);

  const handleRun = async () => {
    setLoading(true);
    setStatus('EXECUTING');
    setOutput('');
    setError('');
    
    try {
      const response = await axios.post('http://localhost:5000/api/compile-and-run', {
        code,
        inputs
      });
      
      // Simulate terminal "stream" feel
      if (response.data.output) {
        setOutput(response.data.output);
        setStatus('SUCCESS');
      }
      if (response.data.error) {
        setError(response.data.error);
        setStatus('ERROR');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'System Failure: Backend Connection Lost.');
      setStatus('OFFLINE');
    } finally {
      setLoading(false);
      setTimeout(() => setStatus(prev => prev === 'SUCCESS' || prev === 'ERROR' ? prev : 'READY'), 2000);
    }
  };

  const copyOutput = () => {
    navigator.clipboard.writeText(output || error);
  };

  return (
    <div className="flex flex-col h-screen bg-[#050505] text-gray-300 font-sans selection:bg-blue-500/30">
      {/* Dynamic Background */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden opacity-30">
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-600/20 blur-[120px] rounded-full" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-600/20 blur-[120px] rounded-full" />
      </div>

      {/* Navigation Bar */}
      <nav className="relative z-10 flex items-center justify-between px-8 py-5 border-b border-white/5 bg-black/20 backdrop-blur-md">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3">
            <div className="relative">
              <div className="absolute inset-0 bg-blue-500 blur-md opacity-40 animate-pulse" />
              <div className="relative p-2.5 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 text-white shadow-xl">
                <Cpu size={22} strokeWidth={2.5} />
              </div>
            </div>
            <div>
              <h1 className="text-lg font-black tracking-tighter text-white leading-none">LEXOR <span className="text-blue-400">CORE</span></h1>
              <p className="text-[9px] uppercase tracking-[0.3em] font-bold text-gray-500 mt-1">Industrial Compiler v1.0</p>
            </div>
          </div>
          
          <div className="h-8 w-px bg-white/5 mx-2" />
          
          <div className="hidden md:flex items-center gap-4">
            <button className="text-[10px] font-bold text-white flex items-center gap-2 hover:text-blue-400 transition-colors">
              <FileCode size={14} /> SCRIPTS
            </button>
            <button className="text-[10px] font-bold text-gray-500 flex items-center gap-2 hover:text-white transition-colors">
              <Settings size={14} /> ENGINE
            </button>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className={cn(
            "hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full border text-[10px] font-bold transition-all duration-500",
            status === 'READY' && "border-blue-500/20 text-blue-400 bg-blue-500/5",
            status === 'EXECUTING' && "border-yellow-500/20 text-yellow-400 bg-yellow-500/5 animate-pulse",
            status === 'SUCCESS' && "border-emerald-500/20 text-emerald-400 bg-emerald-500/5",
            status === 'ERROR' && "border-red-500/20 text-red-400 bg-red-500/5"
          )}>
            <div className={cn(
              "w-1.5 h-1.5 rounded-full",
              status === 'READY' && "bg-blue-400",
              status === 'EXECUTING' && "bg-yellow-400",
              status === 'SUCCESS' && "bg-emerald-400",
              status === 'ERROR' && "bg-red-400"
            )} />
            SYSTEM {status}
          </div>

          <button
            onClick={handleRun}
            disabled={loading}
            className={cn(
              "group relative flex items-center gap-2.5 px-8 py-2.5 rounded-xl font-black text-xs uppercase tracking-widest transition-all duration-300",
              loading 
                ? "bg-gray-800 text-gray-500 cursor-not-allowed" 
                : "bg-white text-black hover:bg-blue-500 hover:text-white hover:scale-[1.02] active:scale-[0.98] shadow-2xl shadow-white/5"
            )}
          >
            {loading ? <Zap size={14} className="animate-spin" /> : <Play size={14} fill="currentColor" />}
            Execute
          </button>
        </div>
      </nav>

      {/* Main Workbench */}
      <main className="relative z-10 flex flex-1 overflow-hidden p-6 gap-6">
        {/* Left: Editor Stack */}
        <div className="flex flex-col w-[60%] gap-6">
          <Panel title="System Workbench" icon={Command} className="flex-1">
            <div className="flex-1 bg-[#1e1e1e]/50">
              <Editor
                height="100%"
                defaultLanguage="cpp" // Generic fallback for highlighting
                theme="vs-dark"
                value={code}
                onChange={setCode}
                options={{
                  fontSize: 16,
                  fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
                  minimap: { enabled: false },
                  scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                  lineNumbers: 'on',
                  renderLineHighlight: 'all',
                  padding: { top: 20 },
                  smoothScrolling: true,
                  cursorBlinking: 'smooth',
                  cursorSmoothCaretAnimation: 'on',
                }}
              />
            </div>
          </Panel>

          <Panel title="Manual Inputs" icon={ChevronRight} className="h-40">
            <textarea
              value={inputs}
              onChange={(e) => setInputs(e.target.value)}
              className="flex-1 p-6 bg-transparent font-mono text-base resize-none focus:outline-none text-blue-300 placeholder:text-white/10"
              placeholder="Enter runtime inputs..."
              spellCheck="false"
            />
          </Panel>
        </div>

        {/* Right: Terminal Stack */}
        <div className="flex flex-col w-[40%] gap-6">
          <Panel title="Kernel Console" icon={Terminal} className="flex-1 bg-black/60">
            <div 
              ref={outputRef}
              className="flex-1 p-8 font-mono text-base overflow-auto custom-scrollbar space-y-6"
            >
              <AnimatePresence mode="popLayout">
                {error ? (
                  <motion.div 
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    key="error"
                    className="space-y-3"
                  >
                    <div className="flex items-center gap-2 text-red-400 text-[10px] font-bold uppercase tracking-tighter">
                      <AlertCircle size={14} className="animate-pulse" /> Kernel Panic / Runtime Exception
                    </div>
                    <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 backdrop-blur-sm relative overflow-hidden group">
                      <div className="absolute inset-0 bg-red-500/5 animate-pulse" />
                      <pre className="relative text-red-400 whitespace-pre-wrap leading-relaxed text-sm">
                        {error}
                      </pre>
                    </div>
                  </motion.div>
                ) : output ? (
                  <motion.div 
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    key="output"
                    className="space-y-2"
                  >
                    <div className="flex items-center gap-2 text-emerald-400/50 text-[10px] font-bold mb-2">
                      <CheckCircle2 size={12} /> STDOUT_STREAM
                    </div>
                    <pre className="text-emerald-400 leading-relaxed drop-shadow-[0_0_8px_rgba(52,211,153,0.3)] whitespace-pre-wrap">
                      {output}
                    </pre>
                  </motion.div>
                ) : !loading && (
                  <motion.div 
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 0.3 }}
                    className="h-full flex flex-col items-center justify-center text-white space-y-4"
                  >
                    <Terminal size={40} strokeWidth={1} />
                    <p className="text-[10px] font-bold tracking-[0.3em] uppercase">System Idle // Awaiting Input</p>
                  </motion.div>
                )}
                
                {loading && (
                  <motion.div 
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="flex flex-col gap-3"
                  >
                    <div className="flex items-center gap-2 text-blue-400 text-xs font-bold uppercase tracking-widest">
                      <Zap size={14} className="animate-bounce" /> Executing Pipeline...
                    </div>
                    <div className="w-full h-1 bg-white/5 rounded-full overflow-hidden">
                      <motion.div 
                        initial={{ x: "-100%" }}
                        animate={{ x: "100%" }}
                        transition={{ repeat: Infinity, duration: 1.5, ease: "easeInOut" }}
                        className="w-1/2 h-full bg-gradient-to-r from-transparent via-blue-500 to-transparent"
                      />
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
            
            <div className="p-4 bg-white/5 flex items-center justify-between border-t border-white/5">
              <span className="text-[9px] font-bold text-white/20 uppercase tracking-widest">Buffer: 4096KB</span>
              <div className="flex gap-2">
                <button 
                  onClick={copyOutput}
                  className="p-2 rounded-lg hover:bg-white/5 text-gray-500 hover:text-white transition-all"
                  title="Copy Output"
                >
                  <Copy size={14} />
                </button>
                <button 
                  onClick={() => { setOutput(''); setError(''); }}
                  className="p-2 rounded-lg hover:bg-white/5 text-gray-500 hover:text-red-400 transition-all"
                  title="Clear Console"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          </Panel>
        </div>
      </main>

      {/* Grid Overlay */}
      <div className="fixed inset-0 pointer-events-none z-0 opacity-[0.03] bg-[url('https://grainy-gradients.vercel.app/noise.svg')] bg-repeat" />
    </div>
  );
};

export default App;
