package net.endrealm.lostsouls.export;

class ChainEnd<T> extends Process<T> {
    private ChainEnd<T> chainEnd;
    @Override
    public void accept(T t) {
        if(chainEnd != null)
            chainEnd.accept(t);
    }

    @Override
    public void setNext(Process<T> next) {
        if(next instanceof ChainEnd) {
            chainEnd = (ChainEnd<T>) next;
        }
    }
}
