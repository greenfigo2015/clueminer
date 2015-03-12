package org.clueminer.eval.external;

import org.clueminer.fixtures.clustering.FakeClustering;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class FmeasureTest extends ExternalTest {

    public FmeasureTest() {
        subject = new Fmeasure();
    }

    /**
     * Test of score method, of class F-measure.
     */
    @Test
    public void testScore_Clustering_Dataset() {
        double score;
        //each cluster should have this scores:
        //Cabernet = 0.7200
        //Syrah = 0.5555
        //Pinot = 0.7272
        score = measure(FakeClustering.wineClustering(), FakeClustering.wineCorrect(), 0.48132780082987553);

        //when using class labels result should be the same
        measure(FakeClustering.wineClustering(), score);
    }

    @Test
    public void testOneClassPerCluster() {
        assertEquals(0.0, subject.score(oneClassPerCluster()), delta);
    }

    @Test
    public void testMostlyWrong() {
        double score = subject.score(FakeClustering.irisMostlyWrong());
        System.out.println("f-measure (mw) = " + score);
        assertEquals(true, score < 0.5);
    }
}
