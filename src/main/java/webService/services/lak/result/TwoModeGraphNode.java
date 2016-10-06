package webService.services.lak.result;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphNode {
    TwoModeGraphNodeType type;
    String uri;
    String displayName;
    
    public TwoModeGraphNode(TwoModeGraphNodeType type, String uri, String displayName) {
        this.type = type;
        this.uri = uri;
        this.displayName = displayName;
    }
    
    public String getUri() {
        return this.uri;
    }
    public TwoModeGraphNodeType getType() {
        return this.type;
    }
    @Override
    public String toString() {
        return "{" + this.uri  + "}";
    }
}