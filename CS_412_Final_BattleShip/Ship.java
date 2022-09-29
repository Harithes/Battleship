public class Ship
{
    int size;
    int positionLetter;
    int positionNumber;
    String firstPosition;
    String lastPostition;

    int setCoordsVert(int positionLetter)
    {
        if(size + positionNumber > 9)
        {
            return positionNumber - size;
        }else
            {
                return size + positionNumber;
            }
    }
    int setCoordsHoriz(int positionNumber)
    {
        if(size + positionLetter > 9)
        {
            return positionLetter - (size - 1);
        }else
            {
                return (size - 1) + positionLetter;
            }
    }
}