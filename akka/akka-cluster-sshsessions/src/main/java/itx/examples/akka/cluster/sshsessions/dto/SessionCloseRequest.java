package itx.examples.akka.cluster.sshsessions.dto;

import java.io.Serializable;

/**
 * Created by juraj on 3/18/17.
 */
public class SessionCloseRequest implements Serializable {

    private String clientId;
    private String sessionActorAddress;

    public SessionCloseRequest(String clientId, String sessionActorAddress) {
        this.clientId = clientId;
        this.sessionActorAddress = sessionActorAddress;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSessionActorAddress() {
        return sessionActorAddress;
    }

}
