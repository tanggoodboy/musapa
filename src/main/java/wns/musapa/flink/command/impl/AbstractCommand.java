package wns.musapa.flink.command.impl;

import wns.musapa.flink.command.Command;

public abstract class AbstractCommand implements Command {
    protected Handler handler;

    public AbstractCommand(Handler handler) {
        this.handler = handler;
    }

    @Override
    public Handler getHandler() {
        return this.handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
