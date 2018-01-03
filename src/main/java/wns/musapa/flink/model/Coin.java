package wns.musapa.flink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Coin {
    private static final ObjectMapper mapper = new ObjectMapper();
    @JsonIgnore
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }

    private CoinCode code;
    private Map<String, Object> extras;

    public Coin(CoinCode code) {
        this.code = code;
    }

    public CoinCode getCode() {
        return code;
    }

    public void setCode(CoinCode code) {
        this.code = code;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    public void addExtra(String key, Object val) {
        if (this.extras == null) {
            this.extras = new HashMap<>(); // lazy initialization
        }
        this.extras.put(key, val);
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
