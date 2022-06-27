# Gatheral's SVI Option Surface calculation.

This Option Pricing implementation calculates Option prices using Gatheral's Stochastic Volatility Inspired approach.

This approach guarantees no arbitrage across time and price.

The theory of SVI approach has been further expanded by showing convergence in the limit to Heston's equations as T
approaches ∞

### TODO

This code was written back when JDK 5 was just becoming popular. Date functionality should be updated to the newer JDK
Date functionality and tweaking it for performance.

### Caveats

This is a snippet of a much larger trading program. Consequently, many features not used in this code base are used
elsewhere. Also, Unit tests and data are not replicated here because the underlying data is proprietary
to <a href="https://www.livevol.com/">LiveVol</a>

### Reference

For equations with rationale for in SVI see:

<https://www.tandfonline.com/doi/abs/10.1080/14697688.2013.819986>



 