package dopo.domain;

/**
 * Representa cualquier elemento que tenga una posición en el tablero.
 */
public interface BoardEntity {
    int getX();
    int getY();
    void setX(int x);
    void setY(int y);
    int getSize();
}
