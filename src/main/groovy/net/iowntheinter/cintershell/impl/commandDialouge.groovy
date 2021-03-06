package net.iowntheinter.cintershell.impl

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.spi.launcher.Command
import io.vertx.ext.shell.command.CommandProcess
import io.vertx.ext.shell.session.Session
import io.vertx.ext.shell.command.CommandBuilder
import io.vertx.ext.shell.system.Process
import io.vertx.ext.shell.command.CommandRegistry


import javax.xml.ws.AsyncHandler
//docs: leave refrence (url or magnet link) to key:documentation
//have ? fetch the dox and display them

/**
 * Created by grant on 11/6/15.
 */
class commandDialouge {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    Vertx vertx
    Closure QuestionsLoader
    Map QuestionsResponses;
    Map ansAr = [:];
    Closure finish
    Logger log
    String intro = "";

    commandDialouge(v, String name, String intr = "", Closure questions, Map respHdl, Closure finishAction) {
        vertx = v as Vertx
        QuestionsLoader = questions  //this is a hack, need to figure out a clean way to get the inital key set
        finish = finishAction;
        intro = intr
        QuestionsResponses = respHdl;
        def builder = CommandBuilder.command(name)
        builder.processHandler(hdlr)
        // Register the command
        def registry = CommandRegistry.getShared(vertx)
        registry.registerCommand(builder.build(vertx))
        log = LoggerFactory.getLogger("shadowSetup cmd")
    }


    def hdlr = { CommandProcess pr ->
        def session = pr.session() as Session
        session.put('DiagCounter', 0)
        session.put('Args', pr.args())
        session.put('ansAr', ansAr)
        session.put('QuestionsResponses', QuestionsResponses)

        QuestionsLoader(pr, { QuestionLoaderContext  ->
            def process = QuestionLoaderContext.process as CommandProcess
            def Questions = QuestionLoaderContext.questions as Map
            def QuestionKeySet = QuestionLoaderContext.questions.keySet()
            session.put('Questions', Questions)
            session.put('QuestionKeySet', QuestionKeySet)

            def buff = Buffer.buffer();
            process.write(intro)

            process.stdinHandler({ keyUp ->  // on each key press, fire this
                if (keyUp != '\n' && keyUp != '\r') {
                    buff.appendString(keyUp)
                    if(!session.get('passwordMode'))
                        process.write(keyUp)
                } else {
                    def QuestionsResponses = session.get('QuestionsResponses') as Map
                    Closure chk = QuestionsResponses[QuestionKeySet[session.get('DiagCounter') as int]]
                    session.put('resp', buff.toString())
                    buff = Buffer.buffer()
                    chk([process: process,resp:session.get('resp')], { ctx ->
                        def InnerAnswerSet = session.get('ansAr') as Map
                        def InnerDiagCtr = session.get('DiagCounter') as int
                        if (ctx['valid']) {
                            InnerAnswerSet[QuestionKeySet[InnerDiagCtr]] = session.get('resp')
                            session.put('ansAr', InnerAnswerSet)
                            log.info(ANSI_WHITE + "\nReceived ${InnerAnswerSet[QuestionKeySet[InnerDiagCtr]]}" + ANSI_RESET)

                            InnerDiagCtr++
                            session.put('DiagCounter', InnerDiagCtr)
                            if (InnerDiagCtr > Questions.size() - 1) {
                                log.trace('\n shell qa collected: ' + session.get('ansAr') + '\n')
                                finish([v: vertx, p: process, d: session.get('ansAr')])
                            } else {
                                ctx.process.write('\n' + ANSI_CYAN + Questions[QuestionKeySet[InnerDiagCtr]] + ANSI_RESET)
                            }
                        } else {
                            ctx.process.write('\n answer did not validatate \n');
                            ctx.process.write('\n' + ANSI_CYAN + Questions[QuestionKeySet[InnerDiagCtr]] + ANSI_RESET)
                        }
                    })

                }
            })

            //dialogctr should always be zero here

            process.write(ANSI_CYAN + Questions[QuestionKeySet[0]] + ANSI_RESET)

        })
    }

}
