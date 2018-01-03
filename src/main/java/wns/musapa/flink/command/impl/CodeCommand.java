package wns.musapa.flink.command.impl;


import wns.musapa.flink.bot.Reporter;
import wns.musapa.flink.command.Command;
import wns.musapa.flink.model.code.UpbitCoinCode;
import wns.musapa.flink.user.User;

public class CodeCommand extends AbstractCommand implements Command.Handler{
    public CodeCommand() {
        super(null);
        setHandler(this);
    }

    @Override
    public String getHelp() {
        return "Prints a list of available coin codes";
    }

    @Override
    public String getCommand() {
        return "/code";
    }

    @Override
    public void handle(Command command, String[] tokens, User user, Reporter reporter) {
        UpbitCoinCode[] codes = UpbitCoinCode.values();
        StringBuilder sb = new StringBuilder();
        for (UpbitCoinCode code : codes) {
            sb.append(String.format("%s(%s)\n", code.getKorean(), code.name()));
        }
        reporter.send(user, sb.toString());
    }
}
