package net.iowntheinter.cintershell.impl

import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.shell.ShellService
import io.vertx.ext.shell.ShellServiceOptions
/**
 * Created by grant on 10/28/15.
 */
class OperatorConsole {
    def vertx
    def logger = LoggerFactory.getLogger("OperatorConsole")
    def shadowmsg
    def config
    ShellService service
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";


    OperatorConsole(Vertx vx, ShellServiceOptions sso) {
        vertx = vx as Vertx
        config = vertx.getOrCreateContext().config()
        logger.info("attempting to start  service ")

        loader({
            sso.setWelcomeMessage(ANSI_RED + shadowmsg + ANSI_RESET)
            service = ShellService.create(vx, sso)

            try {
                service.start({ r ->
                    if (r.succeeded()) {
                        logger.info("result of starting shell service: " + r.result())
                    } else {
                        logger.error("shell service may not be running: ${r.cause()}")
                    }
                })
            } catch (e) {
                logger.error("could not start shell server: ${e}")
                e.printStackTrace()

            }
        })
    }
    void stop(){
        service.stop()
    }
    void loader(cb) {
        try {
            def bfile = "_branding.txt"
            def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
            def bpth = classloader.findResource(bfile);
            logger.info("bfile ref: ${bpth}\n")

            shadowmsg = classloader.getResourceAsStream(bfile).getText()
          //  logger.info("${ANSI_RED + shadowmsg + ANSI_RESET}")
        } catch (nobranding) {
            logger.error("no branding detected, ${nobranding}")
            try {
                def bfile = "cintershell-banner.txt"
                def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
                def bpth = classloader.findResource(bfile);
                logger.info("bfile ref: ${bpth}\n")

                shadowmsg = classloader.getResourceAsStream(bfile).getText()
                logger.info("${ANSI_RED + shadowmsg + ANSI_RESET}")
            } catch (e) {
                logger.error("could not get banner, ${e}")
                shadowmsg = "Welcome to cintershell \n"
            }
        }
        cb()
    }



}

