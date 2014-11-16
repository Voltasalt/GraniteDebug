package co.voltasalt.granitedebug;

import com.google.common.reflect.ClassPath;
import groovy.lang.GroovyRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.granitemc.granite.api.command.Command;
import org.granitemc.granite.api.command.CommandInfo;
import org.granitemc.granite.api.plugin.Plugin;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Plugin(name = "GraniteDebug", id = "granitedebug", version = "1.0")
public class GraniteDebug {
    Map<String, ScriptEngine> scriptEngines = new HashMap<String, ScriptEngine>();

    @Command(name = "eval", info = "Runs a command", aliases = {})
    public void eval(CommandInfo info) throws IllegalAccessException, ScriptException, NoSuchFieldException, IOException {
        ScriptEngine engine;
        if (scriptEngines.containsKey(info.getCommandSender().getName())) {
            engine = scriptEngines.get(info.getCommandSender().getName());
        } else {
            ScriptEngineManager sem = new ScriptEngineManager();
            engine = sem.getEngineByName("groovy");

            scriptEngines.put(info.getCommandSender().getName(), engine);
        }

        engine.getContext().setAttribute("me", info.getCommandSender(), ScriptContext.ENGINE_SCOPE);
        try {
            Object res = run(engine, StringUtils.join(info.getArgs(), " "));
            info.getCommandSender().sendMessage(String.valueOf(res));
        } catch (ScriptException e) {
            handleError(e, info);
        }
    }

    public Object run(ScriptEngine engine, String code) throws IOException, ScriptException {
        String data = "";
        for (ClassPath.ClassInfo clazz : ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClassesRecursive("org.granitemc.granite")) {
            data += "import " + clazz.getName() + "\n";
        }
        data += code;
        return engine.eval(data);
    }

    public void handleError(Throwable t, CommandInfo info) {
        if (t instanceof ScriptException) {
            handleError(t.getCause(), info);
        } else if (t instanceof GroovyRuntimeException) {
            GroovyRuntimeException gre = (GroovyRuntimeException) t;
            if (gre.getNode() != null) {
                info.getCommandSender().sendMessage("(" + gre.getNode().getLineNumber() + ";" + gre.getNode().getColumnNumber() + ") " + gre.getMessageWithoutLocationText());
            } else {
                info.getCommandSender().sendMessage(gre.getMessage());
            }
        } else {
            info.getCommandSender().sendMessage(t.getMessage());
        }
    }
}
