import { TrendingUp, TrendingDown } from 'lucide-react'
import { formatPrice } from '../../utils/currency'

function TickerTape({ liveStocks, currency = 'USD' }) {
  // Get top 15 stocks to scroll (if we don't have enough, duplicate to fill)
  const symbols = Object.keys(liveStocks)
  if (symbols.length === 0) {
    return (
      <div className="w-full bg-[#0B0E14] border-b border-gray-800 h-10 flex items-center justify-center">
        <span className="text-gray-500 text-sm">Connecting to live market data...</span>
      </div>
    )
  }

  // We need enough items to fill the screen twice so the infinite scroll is seamless.
  // We'll duplicate the array if there are less than 15 items.
  let displayStocks = Object.values(liveStocks)
  if (displayStocks.length < 15) {
    displayStocks = [...displayStocks, ...displayStocks, ...displayStocks].slice(0, 15)
  } else {
    displayStocks = displayStocks.slice(0, 15)
  }

  return (
    <div className="w-full bg-[#0B0E14] border-b border-gray-800 overflow-hidden h-10 flex items-center relative z-10">
      <div className="flex whitespace-nowrap animate-marquee">
        {/* We render the same block twice to create a seamless infinite loop */}
        {[1, 2].map((blockId) => (
          <div key={blockId} className="flex items-center">
            {displayStocks.map((stock, i) => {
              const isPositive = (stock.priceChange ?? 0) >= 0
              const color = isPositive ? 'text-green-500' : 'text-red-500'
              
              return (
                <div key={`${blockId}-${stock.symbol}-${i}`} className="flex items-center gap-3 px-6 border-r border-gray-800">
                  <span className="text-gray-300 font-semibold text-sm">{stock.symbol}</span>
                  <span className="text-white font-mono text-sm">{formatPrice(stock.currentPrice, currency)}</span>
                  <div className={`flex items-center gap-1 text-xs font-mono ${color}`}>
                    {isPositive ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
                    {isPositive ? '+' : ''}{stock.priceChangePercent?.toFixed(2) ?? '--'}%
                  </div>
                </div>
              )
            })}
          </div>
        ))}
      </div>
    </div>
  )
}

export default TickerTape
