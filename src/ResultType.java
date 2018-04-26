import java.util.Collections;
import java.util.List;

/**
 * another ResultType for basically get() and diff() method, so we can clarify what the results exactly are by carrying more info
 */
public class ResultType {

    int code;
    List<String> result;

    /**
     * Constructor
     *
     * @param code: use int value to present different status
     */
    public ResultType(int code) {
            this.code = code;
            this.result = Collections.emptyList();
        }

    /**
     * method to set result list when code shows results are valid
     * @param result: valid result list to replace original empty one
     */
    public void setResult(List<String> result) {
            this.result = result;
        }

    /**
     * method to get result list
     *
     * @return reslut list
     */
    public List<String> getResult() {
            return this.result;
        }

}
