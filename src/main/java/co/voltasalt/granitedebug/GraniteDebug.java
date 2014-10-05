package co.voltasalt.granitedebug;

import org.apache.commons.lang3.StringUtils;
import org.granitemc.granite.api.command.Command;
import org.granitemc.granite.api.command.CommandInfo;
import org.granitemc.granite.api.plugin.Plugin;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;


@Plugin(name="GraniteDebug", id="granitedebug", version="1.0")
public class GraniteDebug {
    Map<String, ScriptEngine> scriptEngines = new HashMap<String, ScriptEngine>();
    @Command(name="eval", info="Runs a command", aliases={})
    public void eval(CommandInfo info) {
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
            info.getCommandSender().sendMessage(String.valueOf(engine.eval(StringUtils.join(info.getArgs(), " "))));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
