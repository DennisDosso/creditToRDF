package it.unipd.dei.ims.credittordf.utils;

import org.eclipse.rdf4j.model.base.AbstractIRI;

public class CustomURI extends AbstractIRI  {

    private static final long serialVersionUID = 1692436252019169159L;
    private String stringValue;
    private String namespace;
    private String localName;

    public CustomURI(String nmspc, String lN) {
        this.namespace = nmspc;
        this.localName = lN;
        this.stringValue = nmspc + lN;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public String stringValue() {
        return stringValue;
    }

    public void set(String nmspc, String lN) {
        this.stringValue = (nmspc + lN).toString();
    }


}
