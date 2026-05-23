package dopo.domain.elements;

/** Standard yellow coin. Must collect all to complete level. */
public class YellowCoin extends Coin {
    public YellowCoin(int x, int y) {
        super(x, y, CoinType.YELLOW);
    }
}
