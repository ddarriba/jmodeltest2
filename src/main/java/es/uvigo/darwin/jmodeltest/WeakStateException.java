package es.uvigo.darwin.jmodeltest;

public class WeakStateException extends RuntimeException {

	private static final long serialVersionUID = 201104031313L;

	public WeakStateException(String message) {
		super(message);
	}
	
	public static class UninitializedCriterionException extends WeakStateException {
	
		private static final long serialVersionUID = 201104031313L;
		
		public UninitializedCriterionException(String criterion) {
			super("Attempting to get an uninitialized criterion: " + criterion); 
			}

	}
}
