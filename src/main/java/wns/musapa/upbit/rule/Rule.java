package wns.musapa.upbit.rule;

public interface Rule {
    boolean isFired(double actual);

    interface Handler{
        String handle();
    }
}
