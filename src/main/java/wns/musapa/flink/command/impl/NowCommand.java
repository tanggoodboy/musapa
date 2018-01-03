package wns.musapa.flink.command.impl;

public class NowCommand extends AbstractCommand {
    public NowCommand(Handler handler) {
        super(handler);
    }

    @Override
    public String getHelp() {
        return "Prints a list of coins with highest and lowest rates of change.";
    }

    @Override
    public String getCommand() {
        return "/now";
    }
}
