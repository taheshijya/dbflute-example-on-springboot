package org.docksidestage.app.web.login;

import org.docksidestage.unit.UnitContainerTestCase;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * @author jflute
 */
public class LoginServiceTest extends UnitContainerTestCase {

    public void test_loadUserByUsername() {
        // ## Arrange ##
        LoginService service = new LoginService();
        inject(service);

        // ## Act ##
        UserDetails details = service.loadUserByUsername("Pixy");

        // ## Assert ##
        log(details);
        assertEquals("Pixy", details.getUsername());
    }

    public void test_passwordEncoder() {
        // ## Arrange ##
        PasswordEncoder passwordEncoder = new StandardPasswordEncoder();

        String password = "sea";
        String digest = passwordEncoder.encode(password);
        System.out.println("ハッシュ値 = " + digest);

        if (passwordEncoder.matches(password, digest)) {
            System.out.println("一致したよ");
            return;
        }
        System.out.println("一致しなかったよ");

    }

}
