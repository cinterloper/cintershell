package net.iowntheinter.cintershell.impl

import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.net.JksOptions
import io.vertx.ext.auth.shiro.ShiroAuthRealmType
import io.vertx.ext.shell.ShellServiceOptions
import io.vertx.ext.auth.shiro.ShiroAuthOptions
import io.vertx.ext.shell.ShellServiceOptionsConverter
import io.vertx.ext.shell.term.SSHTermOptions
import io.vertx.core.Vertx
import io.vertx.ext.shell.ShellService
import io.vertx.ext.auth.shiro.LDAPProviderConstants

/**
 * Created by grant on 10/28/15.
 */
class SSHOperatorConsole {
    def cmdset
    def vertx
    def logger = LoggerFactory.getLogger("SSHOperatorConsole")
    def sshOpts = new SSHTermOptions()
    def shadowmsg
    def config
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";




    SSHOperatorConsole(Vertx vx,  ShellServiceOptions authOpts) {
        vertx = vx as Vertx
        config = vertx.getOrCreateContext().config()
        logger.info("attermpting to start ssh service on ${authOpts.getSSHOptions().port}")

        loader({
            authOpts.setWelcomeMessage(ANSI_RED + shadowmsg + ANSI_RESET)
            def service = ShellService.create(vx, authOpts)

            try {
                service.start({ r ->
                    if (r.succeeded()) {
                        logger.info("shell service is running on ${authOpts.getSSHOptions().port}:${r.result()}")
                        logger.info(service)
                    } else {
                        logger.error("shell service may not be running: ${r.cause()}")
                    }
                })
            } catch (e) {
                logger.error("could not start ssh server: ${e}")

            }
        })
    }
    void loader(cb) {
        try {
            def bfile = "cintershell-branding.txt"
            def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
            def bpth = classloader.findResource(bfile);
            logger.info("bfile ref: ${bpth}\n")

            shadowmsg = classloader.getResourceAsStream(bfile).getText()
            logger.info("${ANSI_RED + shadowmsg + ANSI_RESET}")
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

