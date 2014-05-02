package hobby.app;

/**
 * Created by Papucs on 2014.04.20..
 */
public class ModeNotAvailableException extends Exception
{
    public ModeNotAvailableException()
    {
    }

    public ModeNotAvailableException(String message)
    {
        super (message);
    }

    public ModeNotAvailableException(Throwable cause)
    {
        super (cause);
    }

    public ModeNotAvailableException(String message, Throwable cause)
    {
        super (message, cause);
    }
}
