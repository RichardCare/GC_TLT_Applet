package gimbalcom.rpc;


public class DummyRemoteIntegerFactory implements RemoteFactory {

    @Override
    public RemoteFactory.Integer create(String name) {
        return new DummyRemoteInteger(name);
    }

    class DummyRemoteInteger implements RemoteFactory.Integer {

        DummyRemoteInteger(String name) {
            // TODO
        }

        @Override
        public int read_int() {
            return 0;
        }

        @Override
        public void write(int value) {
            // TODO
        }

    }
}
