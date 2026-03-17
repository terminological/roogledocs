# R6 RFuture class

This implements functions to check on the status of a Java process
running in a thread and determine if it has finished, or cancel it if
needs be. This is low level as async classes go as the main goal is to
put the java process into a state in which interruptions in the R
process can cancel the Java thread.

## Details

no details

## Public fields

- `.r6obj`:

  the r6obj creating this future.

- `.api`:

  the java api.

- `.method`:

  the method name that is being called asynchronously

- `.jthread`:

  internal pointer to the rJava reference to the java threadrunner.

- `.converter`:

  converts the Java return value to an R object

- `.returnSig`:

  the return signature of the expected object.

## Methods

### Public methods

- [`RFuture$new()`](#method-RFuture-new)

- [`RFuture$cancel()`](#method-RFuture-cancel)

- [`RFuture$isCancelled()`](#method-RFuture-isCancelled)

- [`RFuture$isDone()`](#method-RFuture-isDone)

- [`RFuture$get()`](#method-RFuture-get)

- [`RFuture$clone()`](#method-RFuture-clone)

------------------------------------------------------------------------

### Method `new()`

Create a new JFuture. This is done automatically by the API.

#### Usage

    RFuture$new(
      r6obj,
      method,
      returnSig,
      converter,
      wrapper = NULL,
      api = r6obj$.api,
      ...
    )

#### Arguments

- `r6obj`:

  The r6obj creating this future, or the class name of a static java
  class

- `method`:

  The java method name to be called async

- `returnSig`:

  The JNI return signature

- `converter`:

  The R6 api function that the return value should use

- `wrapper`:

  The R6 api class that the return value should be wrapped in

- `api`:

  The java api

- `...`:

  parameters to pass to the java method

#### Returns

A new JFuture holding the java thread executing the method.

------------------------------------------------------------------------

### Method `cancel()`

cancel execution of the async function

#### Usage

    RFuture$cancel()

------------------------------------------------------------------------

### Method `isCancelled()`

has the function been cancelled

#### Usage

    RFuture$isCancelled()

------------------------------------------------------------------------

### Method `isDone()`

did execution of the function complete yet (or get cancelled). This can
be used in a while loop. with an appropriate delay.

#### Usage

    RFuture$isDone()

------------------------------------------------------------------------

### Method [`get()`](https://rdrr.io/r/base/get.html)

block execution until the function returns a value or throws an error.

#### Usage

    RFuture$get()

#### Returns

the result of the function or an error.

------------------------------------------------------------------------

### Method `clone()`

The objects of this class are cloneable with this method.

#### Usage

    RFuture$clone(deep = FALSE)

#### Arguments

- `deep`:

  Whether to make a deep clone.
