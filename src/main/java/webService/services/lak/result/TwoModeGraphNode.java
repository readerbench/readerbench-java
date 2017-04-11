package webService.services.lak.result;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphNode {
    private final TwoModeGraphNodeType type;
    private final String uri;
    private final String displayName;
    private boolean active;
    
    public TwoModeGraphNode(TwoModeGraphNodeType type, String uri, String displayName) {
        this.type = type;
        this.uri = uri;
        this.displayName = displayName;
        this.active = true;
    }
    
    public String getUri() {
        return this.uri;
    }
    public TwoModeGraphNodeType getType() {
        return this.type;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "{" + this.uri  + "}";
    }
}