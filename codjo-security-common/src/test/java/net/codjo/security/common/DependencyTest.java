package net.codjo.security.common;
import net.codjo.test.common.depend.Dependency;
import net.codjo.test.common.depend.PackageDependencyTestCase;
/**
 *
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
