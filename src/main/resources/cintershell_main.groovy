// if invoked as a standalone application, this is the entrypoint
// if used as a library, this file may be ignored
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx

import net.iowntheinter.cintershell.impl.SSHOperatorConsole
import net.iowntheinter.cintershell.impl.commandDialouge
import net.iowntheinter.cintershell.impl.InitDiag
def logger = LoggerFactory.getLogger("userInterface")

//register setup command
def soc = new SSHOperatorConsole(vertx, 2244)
v = vertx as Vertx
eb = v.eventBus();


def sn = new commandDialouge(vertx, 'setup', InitDiag.INTRO, InitDiag.QUESTIONS, InitDiag.REACTIONS, InitDiag.FINISH)
