package rca.ac.rw.template.common.response;

import lombok.Getter;
import lombok.Setter;
import rca.ac.rw.template.user.User;

@Getter
@Setter
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private User user;

    public JwtAuthenticationResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}




