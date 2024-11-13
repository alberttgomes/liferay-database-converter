import com.upgrade.tools.convert.MySQLSchemeConverter;
import com.upgrade.tools.util.Print;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

/**
 * @author Albert Gomes Cabral
 */
@Testable
public class MySQLSchemeConverterTest extends MySQLSchemeConverter {

    @BeforeAll
    public static void loadTemplates() {
       Print.info("Initializing tests to %s".formatted(
               MySQLSchemeConverter.class.getName()));
    }

    @Test
    public void testLoadingFilesCase() throws Exception {
        MySQLSchemeConverter mySQLSchemeConverter =
                new MySQLSchemeConverter();

        mySQLSchemeConverter.converter(
                _SOURCE_LIFERAY_SCHEME_SQL, _TARGET_LIFERAY_SCHEME_SQL,
                _NEW_CUSTOMER_SCHEME_OUT_PUT_SQL);

        List<Map<String, String>> contentList =
                _getContentFromFile(
                        _NEW_CUSTOMER_SCHEME_OUT_PUT_SQL,
                        _EXPECTED_CUSTOMER_SCHEME_OUT_PUT_SQL);

        assert contentList != null;

        Assertions.assertEquals(contentList.get(0), contentList.get(1));
    }

    private List<Map<String, String>> _getContentFromFile(
            String newFileOutput, String expectedFileOutput) {
        return null;
    }

    private static final String _EXPECTED_CUSTOMER_SCHEME_OUT_PUT_SQL =
            "expected-customer-scheme-out-put.sql";
    private static final String _NEW_CUSTOMER_SCHEME_OUT_PUT_SQL =
            "new-customer-scheme-out-put.sql";
    private static final String _SOURCE_LIFERAY_SCHEME_SQL =
            "source-liferay-scheme.sql";
    private static final String _TARGET_LIFERAY_SCHEME_SQL =
            "target-customer-scheme.sql";

}
