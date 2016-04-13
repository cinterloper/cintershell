// if invoked as a standalone application, this is the entrypoint
// if used as a library, this file may be ignored
import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.Vertx

import net.iowntheinter.cintershell.impl.SSHOperatorConsole
import net.iowntheinter.cintershell.impl.commandDialouge
import net.iowntheinter.cintershell.impl.cmds.example.TestDiag
import net.iowntheinter.cintershell.impl.commandOneShot
import net.iowntheinter.cintershell.impl.cmds.example.TestOneShot


public class cintershellExample extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        def logger = LoggerFactory.getLogger("userInterface")

//register setup command
        def v = vertx as Vertx
        def eb = v.eventBus();
        def soc = new SSHOperatorConsole(v, 2244)


        def sn = new commandDialouge(v, 'setup', TestDiag.INTRO, TestDiag.QUESTIONS, TestDiag.REACTIONS, TestDiag.FINISH)
        def os = new commandOneShot(v, 'shadowone', TestOneShot.INTRO, TestOneShot.COMMAND, TestOneShot.VALIDATION, TestOneShot.FINISH)
    }

}