package ee3316.intoheart.HTTP;

/**
 * Created by aahung on 3/31/15.
 */
public class Outcome {
    public boolean success;
    public Object object;

    public Outcome(boolean success, Object object) {
        this.success = success;
        this.object = object;
    }

    public String getString() {
        return (String) object;
    }
}
