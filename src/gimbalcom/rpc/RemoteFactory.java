package gimbalcom.rpc;

// Hide RPC implementation

public interface RemoteFactory {

    Integer create(String name);

    public interface Integer {
        public int read_int();
        public void write(int value);
    }
}
