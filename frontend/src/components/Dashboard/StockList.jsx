import { useEffect, useState } from 'react'
import { getAllStocks } from '../../services/api'
import { Activity, TrendingUp, TrendingDown, ChevronRight, Search } from 'lucide-react'
import StockDetailModal from './StockDetailModal'

function StockList({ liveStocks }) {
  const [stocks, setStocks] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedStock, setSelectedStock] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    fetchStocks()
    const interval = setInterval(fetchStocks, 10000)
    return () => clearInterval(interval)
  }, [])

  // Merge live WebSocket updates with fetched stocks
  useEffect(() => {
    if (Object.keys(liveStocks).length > 0) {
      setStocks(prev => prev.map(stock => {
        const live = liveStocks[stock.symbol]
        return live ? { ...stock, ...live } : stock
      }))
    }
  }, [liveStocks])

  // Keep the selected stock updated with latest live data too
  useEffect(() => {
    if (selectedStock) {
      setSelectedStock(prev => {
        const live = liveStocks[prev.symbol]
        return live ? { ...prev, ...live } : prev
      })
    }
  }, [liveStocks])

  const fetchStocks = async () => {
    try {
      const response = await getAllStocks()
      setStocks(response.data)
    } catch (error) {
      console.error('Error fetching stocks:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-400">Loading stocks...</div>
      </div>
    )
  }

  const filteredStocks = stocks.filter(stock => 
    stock.symbol.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <>
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-6">
            <h2 className="text-white text-lg font-semibold">
              Live Stock Prices
            </h2>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
              <input
                type="text"
                placeholder="Search coins or stocks..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="bg-gray-900 border border-gray-800 text-white text-sm rounded-lg pl-9 pr-4 py-2 focus:outline-none focus:border-green-500 transition-colors w-64"
              />
            </div>
          </div>
          <span className="text-gray-500 text-sm hidden sm:inline">
            {filteredStocks.length} tracked · <span className="text-gray-600">click a row for chart</span>
          </span>
        </div>

        {stocks.length === 0 ? (
          <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
            <Activity className="text-gray-600 mx-auto mb-3" size={40} />
            <p className="text-gray-400">
              Waiting for live data from Finnhub...
            </p>
            <p className="text-gray-600 text-sm mt-1">
              Make sure your backend is running and Finnhub API key is set
            </p>
          </div>
        ) : (
          <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-800">
                  <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">SYMBOL</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">PRICE</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">CHANGE</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">VOLUME</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">RSI</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">P/E</th>
                  <th className="w-8"></th>
                </tr>
              </thead>
              <tbody>
                {filteredStocks.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center text-gray-500 py-8">
                      No results found for "{searchQuery}"
                    </td>
                  </tr>
                ) : filteredStocks.map((stock) => {
                  const isPositive = (stock.priceChange ?? 0) >= 0
                  const isSelected = selectedStock?.symbol === stock.symbol
                  return (
                    <tr
                      key={stock.symbol}
                      onClick={() => setSelectedStock(stock)}
                      className={`border-b border-gray-800/50 transition-all cursor-pointer group ${
                        isSelected
                          ? 'bg-green-950/30 border-l-2 border-l-green-500'
                          : liveStocks[stock.symbol]
                          ? 'bg-green-950/20 hover:bg-gray-800/50'
                          : 'hover:bg-gray-800/50'
                      }`}
                    >
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <div
                            className="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold"
                            style={{
                              background: isPositive ? '#14532d' : '#450a0a',
                              color: isPositive ? '#22c55e' : '#ef4444'
                            }}
                          >
                            {stock.symbol.slice(0, 2)}
                          </div>
                          <span className="text-white font-semibold text-sm">
                            {stock.symbol}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <span className="text-white text-sm font-mono">
                          ${stock.currentPrice?.toFixed(2) ?? '--'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className={`flex items-center justify-end gap-1 text-sm ${
                          isPositive ? 'text-green-400' : 'text-red-400'
                        }`}>
                          {isPositive
                            ? <TrendingUp size={14} />
                            : <TrendingDown size={14} />
                          }
                          {isPositive ? '+' : ''}{stock.priceChangePercent?.toFixed(2) ?? '--'}%
                        </div>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <span className="text-gray-400 text-sm">
                          {stock.volume?.toLocaleString() ?? '--'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <span className={`text-sm font-medium ${
                          stock.rsi > 70
                            ? 'text-red-400'
                            : stock.rsi < 30
                            ? 'text-green-400'
                            : 'text-gray-300'
                        }`}>
                          {stock.rsi?.toFixed(1) ?? '--'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <span className="text-gray-400 text-sm">
                          {stock.peRatio?.toFixed(1) ?? '--'}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <ChevronRight
                          size={14}
                          className="text-gray-600 group-hover:text-gray-400 transition-colors"
                        />
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Detail modal — portal-style, rendered at bottom of component */}
      {selectedStock && (
        <StockDetailModal
          stock={selectedStock}
          onClose={() => setSelectedStock(null)}
        />
      )}
    </>
  )
}

export default StockList