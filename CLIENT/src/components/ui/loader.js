import * as React from "react";
import { cva } from "class-variance-authority";
import { cn } from "@/lib/utils";

const loaderVariants = cva(
  "fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-xl"
);

const Loader = React.forwardRef(({ className, ...props }, ref) => (
  <div ref={ref} className={cn(loaderVariants(), className)} {...props}>
    <style>
      {`
        @keyframes morph {
          0%, 100% { 
            border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
            transform: scale(1) rotate(0deg);
          }
          33% { 
            border-radius: 40% 60% 70% 30% / 50% 60% 30% 60%;
            transform: scale(1.05) rotate(120deg);
          }
          66% { 
            border-radius: 70% 30% 50% 50% / 30% 60% 40% 70%;
            transform: scale(0.95) rotate(240deg);
          }
        }

        @keyframes float {
          0%, 100% { transform: translateY(0px) scale(1); }
          50% { transform: translateY(-8px) scale(1.02); }
        }

        @keyframes sparkle {
          0%, 100% { opacity: 0; transform: scale(0) rotate(0deg); }
          50% { opacity: 1; transform: scale(1) rotate(180deg); }
        }

        @keyframes textGlow {
          0%, 100% { opacity: 0.8; text-shadow: 0 0 20px rgba(99, 102, 241, 0.3); }
          50% { opacity: 1; text-shadow: 0 0 30px rgba(99, 102, 241, 0.6), 0 0 40px rgba(99, 102, 241, 0.4); }
        }

        @keyframes ripple {
          0% { transform: scale(0); opacity: 1; }
          100% { transform: scale(4); opacity: 0; }
        }

        .animate-morph { animation: morph 4s ease-in-out infinite; }
        .animate-float { animation: float 3s ease-in-out infinite; }
        .animate-sparkle { animation: sparkle 2s ease-in-out infinite; }
        .animate-text-glow { animation: textGlow 2s ease-in-out infinite; }
        .animate-ripple { animation: ripple 2s ease-out infinite; }
      `}
    </style>

    <div className="flex flex-col items-center space-y-12">
      {/* Magical morphing orb with particles */}
      <div className="relative w-32 h-32">
        {/* Ripple effects */}
        <div className="absolute inset-0 border-2 border-indigo-300/30 rounded-full animate-ripple"></div>
        <div className="absolute inset-0 border-2 border-indigo-300/20 rounded-full animate-ripple" style={{ animationDelay: '0.5s' }}></div>
        <div className="absolute inset-0 border-2 border-indigo-300/10 rounded-full animate-ripple" style={{ animationDelay: '1s' }}></div>
        
        {/* Main morphing orb */}
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 animate-morph shadow-2xl">
          <div className="absolute inset-2 bg-gradient-to-tl from-white/20 to-transparent rounded-full blur-sm"></div>
        </div>

        {/* Floating particles */}
        <div className="absolute -top-2 -right-2 w-4 h-4 bg-yellow-400 rounded-full animate-sparkle shadow-lg"></div>
        <div className="absolute -bottom-2 -left-2 w-3 h-3 bg-cyan-400 rounded-full animate-sparkle" style={{ animationDelay: '0.7s' }}></div>
        <div className="absolute -top-4 left-1/2 w-2 h-2 bg-green-400 rounded-full animate-sparkle" style={{ animationDelay: '1.3s' }}></div>
        
        {/* Inner glow */}
        <div className="absolute inset-4 bg-white/10 rounded-full blur-md animate-pulse"></div>
      </div>

      {/* Sophisticated text section */}
      <div className="text-center space-y-6">
        <div className="animate-float">
          <h2 className="text-3xl font-light tracking-wider text-foreground animate-text-glow">
            API Automation
          </h2>
        </div>
        
        <div className="space-y-4">
          <p className="text-lg text-muted-foreground font-light italic -mt-2">
            Processing request, please wait...
          </p>
          
          {/* Elegant progress indicator */}
          <div className="flex justify-center items-center space-x-3 mt-4">
            {[...Array(5)].map((_, i) => (
              <div
                key={i}
                className="w-1.5 h-8 bg-gradient-to-t from-indigo-500 to-purple-400 rounded-full animate-float"
                style={{
                  animationDelay: `${i * 0.15}s`,
                  transform: `scale(${0.8 + (i * 0.1)})`
                }}
              />
            ))}
          </div>

        </div>
      </div>
    </div>
  </div>
));

Loader.displayName = "Loader";

export { Loader };