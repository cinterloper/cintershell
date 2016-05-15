// if invoked as a standalone application, this is the entrypoint
// if used as a library, this file may be ignored
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.Vertx
import io.vertx.ext.auth.shiro.LDAPProviderConstants
import io.vertx.ext.auth.shiro.ShiroAuthOptions
import io.vertx.ext.auth.shiro.ShiroAuthRealmType
import io.vertx.ext.shell.ShellServiceOptions
import io.vertx.ext.shell.ShellServiceOptionsConverter
import net.iowntheinter.cintershell.impl.OperatorConsole as SSHOperatorConsole
import net.iowntheinter.cintershell.impl.commandDialouge
import net.iowntheinter.cintershell.impl.cmds.example.TestDiag
import net.iowntheinter.cintershell.impl.commandOneShot
import net.iowntheinter.cintershell.impl.cmds.example.TestOneShot


public class cintershellExample extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        def  soc
        def port = 2244
        def logger = LoggerFactory.getLogger("userInterface")
        def auth = 'properties'
        def eb = vertx.eventBus();
        def JShiroOptions = new JsonObject()

        if (auth == 'ldap') {
            JShiroOptions.put("type", ShiroAuthRealmType.LDAP)
                    .put("provider", "shiro")
                    .put("config", new JsonObject()
                    .put(LDAPProviderConstants.LDAP_URL, "ldap://localhost:10389")
                    .put(LDAPProviderConstants.LDAP_USER_DN_TEMPLATE_FIELD, "uid={0},ou=system")
                    .put(LDAPProviderConstants.LDAP_AUTHENTICATION_MECHANISM, "simple")
            )

        } else if (auth == 'properties') {
            JShiroOptions.put("type", ShiroAuthRealmType.PROPERTIES)
                    .put("provider", "shiro")
                    .put("config", (new JsonObject().put('properties_path', 'file:example_auth.properties')))

        }


        JsonObject joptions =
                new JsonObject().put("sshOptions",
                        new JsonObject()
                                .put("host", "0.0.0.0")
                                .put("port", port)
                                .put("keyPairOptions",
                                new JsonObject()
                                        .put("path", "keystore.jks")
                                        .put("password", "wibble")
                        )
                                .put("authOptions", JShiroOptions));

        def ShellServiceOptions sso = new ShellServiceOptions();
        ShellServiceOptionsConverter.fromJson(joptions, sso)
        soc = new SSHOperatorConsole(vertx, sso)


        def sn = new commandDialouge(vertx, 'setup', TestDiag.INTRO, TestDiag.QUESTIONS, TestDiag.REACTIONS, TestDiag.FINISH)
        def os = new commandOneShot(vertx, 'shadowone', TestOneShot.INTRO, TestOneShot.COMMAND, TestOneShot.FINISH)
    }

}