package edu.uconn.engr.dna.isoem;

public interface IsoformList {

	void intersect(IsoformList isoformList);

	void reunite(IsoformList isoList);

	double getMultiplicity();

	void setMultiplicity(double m);

	double getQualityScore();

	void setQualityScore(double d);

	boolean isEmpty();

	int size();

	Iterable<Object> keySet();

	Iterable<Entry> entrySet();

	public void setExpectedMultiplicity(double e);
	public double getExpectedMultiplicity();

	public interface Entry {

		String getKey();

		double getValue();

		void setValue(double v);
	}
}
