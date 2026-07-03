import { useEffect, useState } from 'react'
import {
  getAlertRules,
  createAlertRule,
  deleteAlertRule,
  getAlertHistory
} from '../../services/api'
import { Bell, Trash2, Plus, Clock } from 'lucide-react'
import toast from 'react-hot-toast'

function Alerts() {
  const [rules, setRules] = useState([])
  const [history, setHistory] = useState([])
  const [activeTab, setActiveTab] = useState('rules')
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({
    symbol: '',
    indicatorType: 'RSI',
    conditionType: 'GREATER_THAN',
    thresholdValue: ''
  })

  useEffect(() => {
    fetchRules()
    fetchHistory()
  }, [])

  const fetchRules = async () => {
    try {
      const response = await getAlertRules()
      setRules(response.data)
    } catch (error) {
      console.error('Error fetching alert rules:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchHistory = async () => {
    try {
      const response = await getAlertHistory()
      setHistory(response.data)
    } catch (error) {
      console.error('Error fetching alert history:', error)
    }
  }

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createAlertRule({
        ...form,
        thresholdValue: parseFloat(form.thresholdValue)
      })
      toast.success('Alert rule created!')
      setForm({
        symbol: '',
        indicatorType: 'RSI',
        conditionType: 'GREATER_THAN',
        thresholdValue: ''
      })
      fetchRules()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create alert')
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteAlertRule(id)
      toast.success('Alert rule deleted')
      fetchRules()
    } catch (error) {
      toast.error('Failed to delete alert rule')
    }
  }

  return (
    <div>
      <h2 className="text-white text-lg font-semibold mb-4">Alert Engine</h2>

      {/* Create Alert Form */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
        <h3 className="text-white font-medium mb-4 flex items-center gap-2">
          <Plus size={16} className="text-green-400" />
          Create Alert Rule
        </h3>
        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
            <div>
              <label className="text-gray-400 text-xs mb-1 block">Symbol</label>
              <input
                type="text"
                name="symbol"
                value={form.symbol}
                onChange={handleChange}
                required
                placeholder="AAPL"
                className="w-full bg-gray-800 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-green-500 uppercase"
              />
            </div>
            <div>
              <label className="text-gray-400 text-xs mb-1 block">Indicator</label>
              <select
                name="indicatorType"
                value={form.indicatorType}
                onChange={handleChange}
                className="w-full bg-gray-800 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-green-500"
              >
                <option value="RSI">RSI</option>
                <option value="PE_RATIO">P/E Ratio</option>
                <option value="VOLUME">Volume</option>
              </select>
            </div>
            <div>
              <label className="text-gray-400 text-xs mb-1 block">Condition</label>
              <select
                name="conditionType"
                value={form.conditionType}
                onChange={handleChange}
                className="w-full bg-gray-800 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-green-500"
              >
                <option value="GREATER_THAN">Greater Than</option>
                <option value="LESS_THAN">Less Than</option>
              </select>
            </div>
            <div>
              <label className="text-gray-400 text-xs mb-1 block">Threshold</label>
              <input
                type="number"
                name="thresholdValue"
                value={form.thresholdValue}
                onChange={handleChange}
                required
                placeholder="70"
                className="w-full bg-gray-800 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-green-500"
              />
            </div>
          </div>
          <button
            type="submit"
            className="mt-4 bg-green-500 hover:bg-green-600 text-white font-medium px-6 py-2 rounded-lg text-sm transition-colors"
          >
            Create Alert
          </button>
        </form>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-4">
        {['rules', 'history'].map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeTab === tab
                ? 'bg-green-500 text-white'
                : 'bg-gray-800 text-gray-400 hover:text-white'
            }`}
          >
            {tab === 'rules' ? `Active Rules (${rules.length})` : `History (${history.length})`}
          </button>
        ))}
      </div>

      {/* Active Rules */}
      {activeTab === 'rules' && (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          {rules.length === 0 ? (
            <div className="p-12 text-center">
              <Bell className="text-gray-600 mx-auto mb-3" size={40} />
              <p className="text-gray-400">No active alert rules</p>
              <p className="text-gray-600 text-sm mt-1">
                Create a rule above to get started
              </p>
            </div>
          ) : (
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-800">
                  <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">SYMBOL</th>
                  <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">INDICATOR</th>
                  <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">CONDITION</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">THRESHOLD</th>
                  <th className="text-center text-gray-400 text-xs font-medium px-4 py-3">ACTION</th>
                </tr>
              </thead>
              <tbody>
                {rules.map(rule => (
                  <tr key={rule.id} className="border-b border-gray-800/50 hover:bg-gray-800/30">
                    <td className="px-4 py-3">
                      <span className="text-white font-semibold text-sm">{rule.symbol}</span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="bg-blue-500/20 text-blue-400 text-xs px-2 py-0.5 rounded-full">
                        {rule.indicatorType}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-gray-400 text-sm">
                        {rule.conditionType === 'GREATER_THAN' ? '>' : '<'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span className="text-white text-sm">{rule.thresholdValue}</span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button
                        onClick={() => handleDelete(rule.id)}
                        className="text-gray-500 hover:text-red-400 transition-colors"
                      >
                        <Trash2 size={15} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Alert History */}
      {activeTab === 'history' && (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          {history.length === 0 ? (
            <div className="p-12 text-center">
              <Clock className="text-gray-600 mx-auto mb-3" size={40} />
              <p className="text-gray-400">No alerts triggered yet</p>
              <p className="text-gray-600 text-sm mt-1">
                History appears here when your alert rules are triggered
              </p>
            </div>
          ) : (
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-800">
                  <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">SYMBOL</th>
                  <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">MESSAGE</th>
                  <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">TRIGGERED AT</th>
                </tr>
              </thead>
              <tbody>
                {history.map(item => (
                  <tr key={item.id} className="border-b border-gray-800/50 hover:bg-gray-800/30">
                    <td className="px-4 py-3">
                      <span className="text-white font-semibold text-sm">{item.symbol}</span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-gray-400 text-sm">{item.message}</span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span className="text-gray-500 text-xs">
                        {new Date(item.triggeredAt).toLocaleString()}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}

export default Alerts