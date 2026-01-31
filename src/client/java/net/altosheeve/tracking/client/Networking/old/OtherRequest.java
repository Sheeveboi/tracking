package net.altosheeve.soprano.client.Networking.old;

public class OtherRequest {
    protected interface cb { //generic callback method
        boolean cb();
    }
    protected final cb runtimeTask;
    OtherRequest(cb runtime) {
        this.runtimeTask = runtime;
    }
    //if there's no fulfillment task that needs to be done, then a separate lambda does not need to be made and the runtime lambda can be assigned directly to the lambda parameter
    OtherRequest(cb runtime, cb fulfillment) { //two lambdas are passed. One to execute many times while the request is active, and one to execute once for when the request is fulfilled
        this.runtimeTask = () -> {
            boolean out = runtime.cb(); //the runtime lambda returns a boolean, telling the request if it should be considered fulfilled or not
            fulfillment.cb(); //technically the fulfillment lambda also returns a boolean, it's just never used
            return out;
            //its very important that the runtime lambda executes first, because it could be used in a non TBM setting where a separate thread might be created to block the main thread
        };
    }
    public boolean exec() { return this.runtimeTask.cb(); } //main interface in which the request is executed with
}
