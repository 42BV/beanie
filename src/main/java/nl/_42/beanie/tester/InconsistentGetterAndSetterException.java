package nl._42.beanie.tester;

/**
 * Exception thrown when the getter returns a different value
 * than initially set with the setter.
 * 
 * @author Jeroen van Schagen
 */
public class InconsistentGetterAndSetterException extends RuntimeException {

	public InconsistentGetterAndSetterException(String message) {
		super(message);
	}
    
    public InconsistentGetterAndSetterException(String message, Throwable cause) {
        super(message, cause);
    }

}
