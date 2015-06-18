# Welcome to Gridman #

This is an Oracle Coherence Open Source project, all the founders are Coherence Experts working in the London Financial Industry.

One of the main aims of this project is to submit code to the Coherence team in order to improve the core product.  Compare this with the [INCUBATOR](http://coherence.oracle.com/display/INCUBATOR/Home) which is written by Oracle.

## What's here? ##
  * Coherence Security Code
    * ( Kerberos / Active Directory integration ) [here](http://code.google.com/p/gridman/source/browse/#svn/trunk/sandbox/src/main/java/org/gridman/coherence/security/kerberos)
    * Cluster lock ( a simple lock to the cluster with a System Property ) [here](http://code.google.com/p/gridman/source/browse/#svn/trunk/sandbox/src/main/java/org/gridman/coherence/security/clusterLock)
    * Read / Write access to Caches and Invocation Services [here](http://code.google.com/p/gridman/source/browse/#svn/trunk/sandbox/src/main/java/org/gridman/coherence/security/simple)
    * A demo of the functionality [here](http://code.google.com/p/gridman/source/browse/#svn/trunk/sandbox/src/main/java/org/gridman/coherence/security/demo)
    * Coherence Security Utils [here](http://code.google.com/p/gridman/source/browse/trunk/sandbox/src/main/java/org/gridman/coherence/security/simple/CoherenceSecurityUtils.java)
    * Tests for Security functionality [here](http://code.google.com/p/gridman/source/browse/trunk/sandbox/src/test/java/org/gridman/coherence/security/simple/SecurityTest.java)
  * Kerberos GSS JAAS code to support the above (could be useful to others) [here](http://code.google.com/p/gridman/source/browse/trunk/sandbox/src/#src/main/java/org/gridman/kerberos)
  * Classloader Cluster - startup a cluster in a single process using ChildFirstClassloaders for isolation [here](http://code.google.com/p/gridman/source/browse/#svn/trunk/sandbox/src/main/java/org/gridman/classloader)
  * Andrew's presentation to the London Oracle Coherence SIG : [here](http://gridman.googlecode.com/svn/trunk/sandbox/src/test/resources/SIGSecurity.ppt)

## What's coming soon ##
  * BinaryDatabaseCacheStore - a CacheStore which writes the binary directly to the database
  * Index code ( useful classes for checking indexes )
  * SerializerChecker - to check you are not deserializing certain objects on the cluster

## Founder members ##
Founder members are :
  * Andrew Wilson - andrew.wilson@orangephoenix.com
  * Jonathan Knight - jk@thegridman.com
  * Keith Loose - keith@looseflow.com

## How do I join? ##
We are very happy for anyone to get involved.  Just drop us an email with your ideas!