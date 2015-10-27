package edu.uconn.engr.dna.util;

public class Triple<A, B, C> {

	private A a;
	private B b;
    private C c;

	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
        this.c = c;
	}

	public A getFirst() {
		return a;
	}

	public B getSecond() {
		return b;
	}

    public C getThird() {
        return c;
    }

	public void setFirst(A a) {
		this.a = a;
	}

	public void setSecond(B b) {
		this.b = b;
	}

    public void setThird(C c) {
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triple triple = (Triple) o;

        if (a != null ? !a.equals(triple.a) : triple.a != null) return false;
        if (b != null ? !b.equals(triple.b) : triple.b != null) return false;
        if (c != null ? !c.equals(triple.c) : triple.c != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        return result;
    }
}