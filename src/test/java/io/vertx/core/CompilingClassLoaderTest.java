package io.vertx.core;

import io.vertx.core.impl.verticle.CompilingClassLoader;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.verticles.TestVerticle;
import org.apache.maven.shared.invoker.*;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;

public class CompilingClassLoaderTest  extends VertxTestBase {

  public void setUp() throws Exception {
    super.setUp();
    TestVerticle.instanceCount.set(0);
    createMultiReleaseJar();
  }

  private void createMultiReleaseJar() throws MavenInvocationException {
    //System.setProperty("maven.home", "/usr/share/maven");
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File("src/test/resources/multi-release-jar/pom.xml"));
    request.setGoals(Arrays.asList("install"));
    Invoker invoker = new DefaultInvoker();
    InvocationResult result = invoker.execute( request );
    assertEquals(0, result.getExitCode());
  }


  @Test
  public void  compileFileWithImports() throws Exception {
    String className = "MyVerticle";
    File dir = Files.createTempDirectory("vertx").toFile();
    dir.deleteOnExit();
    File source = new File(dir, className + ".java");
    Files.write(source.toPath(), ("import io.fake.eight.Java8Class;\n public class " + className + " extends Java8Class {} ").getBytes());


    File jarFile = new File("src/test/resources/multi-release-jar/multijar-test-0.0.1.jar");
    assertTrue(jarFile.exists());
    URLClassLoader loader = new URLClassLoader(new URL[]{dir.toURI().toURL(), jarFile.toURI().toURL() }, Thread.currentThread().getContextClassLoader());

    CompilingClassLoader compilingClassLoader = new CompilingClassLoader(loader, className + ".java");
    compilingClassLoader.loadClass(className);
  }
}
