package net.codjo.security.server;
import net.codjo.test.common.depend.Dependency;
import net.codjo.test.common.depend.PackageDependencyTestCase;
/**
 * Test les dépendances de cette API en utilisant JDepend.
 */
public class DependencyTest extends PackageDependencyTestCase {

    public void test_dependency() throws Exception {
        Dependency dependency = createDependency();
        dependency.addIgnoredPackage("org.w3c.dom");
        dependency.addIgnoredPackage("org.xml.sax");
        dependency.assertDependency("dependency.txt");
        dependency.assertNoCycle();
    }


    public void test_dependencyTest() throws Exception {
        Dependency dependency = createTestDependency();
        dependency.addIgnoredPackage("org.w3c.dom");
        dependency.addIgnoredPackage("org.xml.sax");
        dependency.assertDependency("dependencyTest.txt");
        dependency.assertNoCycle();
    }
}
