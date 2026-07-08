import { useEffect, useState } from 'react'
import { getScreenerResults } from '../../services/api'
import { AlertTriangle, TrendingUp, TrendingDown, Activity } from 'lucide-react'

function Screener({ screenerUpdates }) {
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all')

  useEffect(() => {
    fetchScreenerResults()
  }, [])

  // Merge live WebSocket screener updates
  useEffect(() => {
    if (screenerUpdates.length > 0) {
      setResults(prev => {
        const updated = [...prev]
        screenerUpdates.forEach(update => {
          const index = updated.findIndex(r => r.symbol === update.symbol)
          if (index >= 0) {
            updated[index] = update
          } else {
            updated.push(update)
          }
        })
        return updated
      })
    }
  }, [screenerUpdates])

  const fetchScreenerResults = async () => {
    try {
      const response = await getScreenerResults()
      setResults(response.data)
    } catch (error) {
      console.error('Error fetching screener results:', error)
    } finally {
      setLoading(false)
    }
  }

  const filteredResults = results.filter(r => {
    if (filter === 'all') return true
    if (filter === 'rsi_overbought') return r.rsiOverbought
    if (filter === 'rsi_oversold') return r.rsiOversold
    if (filter === 'pe_alert') return r.peAlert
    if (filter === 'volume_spike') return r.volumeSpike
    return true
  })

  const filters = [
    { id: 'all', label: 'All Stocks' },
    { id: 'rsi_overbought', label: 'RSI Overbought (>70)' },
    { id: 'rsi_oversold', label: 'RSI Oversold (<30)' },
    { id: 'pe_alert', label: 'High P/E (>15)' },
    { id: 'volume_spike', label: 'Volume Spike' },
  ]

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-400">Loading screener...</div>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-white text-lg font-semibold">Stock Screener</h2>
        <span className="text-gray-500 text-sm">
          {filteredResults.length} results
        </span>
      </div>

      {/* Filter Tabs */}
      <div className="flex gap-2 mb-4 flex-wrap">
        {filters.map(f => (
          <button
            key={f.id}
            onClick={() => setFilter(f.id)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              filter === f.id
                ? 'bg-green-500 text-white'
                : 'bg-gray-800 text-gray-400 hover:text-white'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {filteredResults.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <Activity className="text-gray-600 mx-auto mb-3" size={40} />
          <p className="text-gray-400">No screener results yet</p>
          <p className="text-gray-600 text-sm mt-1">
            Results appear as live data streams in
          </p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-800">
                <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">SYMBOL</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">PRICE</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">RSI</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">P/E</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">VOLUME</th>
                <th className="text-center text-gray-400 text-xs font-medium px-4 py-3">SIGNALS</th>
              </tr>
            </thead>
            <tbody>
              {filteredResults.map((result) => (
                <tr
                  key={result.symbol}
                  className="border-b border-gray-800/50 hover:bg-gray-800/30 transition-colors"
                >
                  <td className="px-4 py-3">
                    <span className="text-white font-semibold text-sm">
                      {result.symbol}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <span className="text-white text-sm">
                      ${result.currentPrice?.toFixed(2) ?? '--'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <span className={`text-sm font-medium ${
                      result.rsiOverbought
                        ? 'text-red-400'
                        : result.rsiOversold
                        ? 'text-green-400'
                        : 'text-gray-300'
                    }`}>
                      {result.rsi?.toFixed(1) ?? '--'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <span className={`text-sm ${
                      result.peAlert ? 'text-yellow-400' : 'text-gray-400'
                    }`}>
                      {result.peRatio?.toFixed(1) ?? '--'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <span className={`text-sm ${
                      result.volumeSpike ? 'text-blue-400' : 'text-gray-400'
                    }`}>
                      {result.volume?.toLocaleString() ?? '--'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-center gap-1 flex-wrap">
                      {result.rsiOverbought && (
                        <span className="bg-red-500/20 text-red-400 text-xs px-2 py-0.5 rounded-full">
                          Overbought
                        </span>
                      )}
                      {result.rsiOversold && (
                        <span className="bg-green-500/20 text-green-400 text-xs px-2 py-0.5 rounded-full">
                          Oversold
                        </span>
                      )}
                      {result.peAlert && (
                        <span className="bg-yellow-500/20 text-yellow-400 text-xs px-2 py-0.5 rounded-full">
                          High P/E
                        </span>
                      )}
                      {result.volumeSpike && (
                        <span className="bg-blue-500/20 text-blue-400 text-xs px-2 py-0.5 rounded-full">
                          Vol Spike
                        </span>
                      )}
                      {!result.rsiOverbought && !result.rsiOversold
                        && !result.peAlert && !result.volumeSpike && (
                        <span className="text-gray-600 text-xs">—</span>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default Screener