package gimbalcom.rpc;
import org.mbed.RPC.HTTPRPC;
import org.mbed.RPC.RPCVariable;


public class RpcRemoteIntegerFactory implements RemoteFactory {

    private final HTTPRPC mbed;

    public RpcRemoteIntegerFactory(HTTPRPC mbed) {
        this.mbed = mbed;
    }

    @Override
    public RemoteFactory.Integer create(String name) {
        return new RpcRemoteInteger(mbed, name);
    }

    class RpcRemoteInteger implements RemoteFactory.Integer {
        private final RPCVariable<java.lang.Integer> delegate;

        RpcRemoteInteger(HTTPRPC mbed, String string) {
            delegate = new RPCVariable<java.lang.Integer>(mbed, string);
        }

        @Override
        public int read_int() {
            return delegate.read_int();
        }

        @Override
        public void write(int value) {
            delegate.write(value);
        }
    }
}
