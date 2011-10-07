package org.gridman.coherence.cachestore;

public class EntityStub {

    private String fieldOne;

    public EntityStub() {
    }

    public String getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityStub that = (EntityStub) o;

        if (fieldOne != null ? !fieldOne.equals(that.fieldOne) : that.fieldOne != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return fieldOne != null ? fieldOne.hashCode() : 0;
    }
}
