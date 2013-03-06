package org.wargamer2010.signshop.operations;

import java.util.Collection;

public class SignShopArgument<E> {
    private E inner;
    private E special;
    private boolean bSpecial = false;
    private SignShopArguments collection;

    public SignShopArgument(SignShopArguments pCollection) {
        collection = pCollection;
    }

    public SignShopArguments getCollection() { 
        return collection; 
    }

    public E get() { 
        return (isSpecial() ? special : inner); 
    }

    public E getRoot() { 
        return inner; 
    }

    public void set(E pSpecial) { 
        setSpecial(true); special = pSpecial; 
    }

    public void setRoot(E pInner) { 
        inner = pInner; 
    }

    public void setSpecial(boolean pSpecial) {
        bSpecial = pSpecial;
    }

    public boolean isSpecial() {
        return bSpecial;
    }

    public boolean isEmpty() {
        if(get() instanceof Collection)
            return ((Collection)get()).isEmpty();
        else if(get() instanceof String)
            return ((String)get()).isEmpty();            
        return false;
    }
}
