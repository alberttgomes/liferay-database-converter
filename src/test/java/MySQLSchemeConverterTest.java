import com.upgrades.tool.convert.SchemeMySQLConverter;
import com.upgrades.tool.util.Print;
import com.upgrades.tool.util.ResultsThreadLocal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.List;
import java.util.Map;

/**
 * @author Albert Gomes Cabral
 */
@Testable
public class SchemeMySQLConverterTest extends SchemeMySQLConverter {

    @BeforeAll
    public static void loadTemplates() {
       Print.info(
              "Initializing tests to " +
                       SchemeMySQLConverter.class.getName());
    }

    @Test
    public void testLoadingFilesCase() throws Exception {

        SchemeMySQLConverter schemeMySQLConverter =
                new SchemeMySQLConverter();

        schemeMySQLConverter.converter(
                _SOURCE_LIFERAY_SCHEME_SQL, _TARGET_LIFERAY_SCHEME_SQL,
                _NEW_CUSTOMER_SCHEME_OUT_PUT_SQL);

        if (ResultsThreadLocal.getResultsThreadLocal()) {
            List<Map<String, String>> contentList =
                    _getContentFromFile(
                            _NEW_CUSTOMER_SCHEME_OUT_PUT_SQL,
                            _EXPECTED_CUSTOMER_SCHEME_OUT_PUT_SQL);

            Assertions.assertEquals(contentList.get(0), contentList.get(1));
        }
        else {
             Print.error(
                     "test testLoadingFilesCase fail", null);
        }

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
