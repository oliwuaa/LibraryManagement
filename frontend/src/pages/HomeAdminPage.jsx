import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/HomeLibPage.css';
import { fetchWithAuth } from '../Api.js';
import GlobalAlert from '../components/GlobalAlert';

const HomeAdminPage = () => {
    const [libraries, setLibraries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchLibrary, setSearchLibrary] = useState('');
    const [editingLibraryId, setEditingLibraryId] = useState(null);
    const [editFormData, setEditFormData] = useState({ name: '', address: '' });
    const [showAddForm, setShowAddForm] = useState(false);
    const [newLibraryData, setNewLibraryData] = useState({ name: '', address: '' });
    const [user, setUser] = useState(null);
    const [alertMsg, setAlertMsg] = useState('');
    const [alertType, setAlertType] = useState('info');

    useEffect(() => {
        const fetchAllData = async () => {
            await Promise.all([fetchLibraries(), fetchUser()]);
            setLoading(false);
        };
        fetchAllData();
    }, []);

    const fetchLibraries = async () => {
        try {
            const res = await fetchWithAuth('/libraries');
            if (!res.ok) throw new Error();
            const data = await res.json();
            setLibraries(data);
        } catch (err) {
            setError('Error loading libraries.');
            console.error(err);
        }
    };

    const fetchUser = async () => {
        try {
            const res = await fetchWithAuth('/users/me');
            if (!res.ok) throw new Error();
            const data = await res.json();
            setUser(data);
        } catch (err) {
            setError('Error loading user.');
            console.error(err);
        }
    };

    const filteredLibraries = libraries.filter(lib =>
        lib.name.toLowerCase().includes(searchLibrary.toLowerCase())
    );

    const handleEdit = (library) => {
        setEditingLibraryId(library.id);
        setEditFormData({ name: library.name, address: library.address });
    };

    const handleEditChange = (field, value) => {
        setEditFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSaveEdit = async () => {
        setAlertMsg('');
        if (!editFormData.name.trim() || !editFormData.address.trim()) {
            setAlertType('error');
            setAlertMsg('Both name and address are required.');
            return;
        }

        try {
            const res = await fetchWithAuth(`/libraries/${editingLibraryId}`, {
                method: 'PUT',
                body: JSON.stringify(editFormData)
            });

            if (res.ok) {
                setEditingLibraryId(null);
                await fetchLibraries();
                setAlertType('success');
                setAlertMsg('Library updated successfully.');
            } else {
                const body = await res.json();
                setAlertType('error');
                setAlertMsg(body.message || 'Failed to update library.');
            }
        } catch (err) {
            setAlertType('error');
            setAlertMsg('Error updating library.');
            console.error(err);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this library?')) return;

        setAlertMsg('');
        try {
            const res = await fetchWithAuth(`/libraries/${id}`, {
                method: 'DELETE'
            });

            if (res.ok) {
                setLibraries(libraries.filter(lib => lib.id !== id));
                setAlertType('success');
                setAlertMsg('Library deleted successfully.');
            } else {
                const body = await res.json();
                setAlertType('error');
                setAlertMsg(body.message || 'Failed to delete library.');
            }
        } catch (err) {
            setAlertType('error');
            setAlertMsg('Error deleting library.');
            console.error(err);
        }
    };

    const handleNewLibraryChange = (field, value) => {
        setNewLibraryData(prev => ({ ...prev, [field]: value }));
    };

    const handleAddLibrary = async () => {
        setAlertMsg('');
        if (!newLibraryData.name.trim() || !newLibraryData.address.trim()) {
            setAlertType('error');
            setAlertMsg('Both name and address are required.');
            return;
        }

        try {
            const res = await fetchWithAuth('/libraries', {
                method: 'POST',
                body: JSON.stringify(newLibraryData)
            });

            if (res.ok) {
                setNewLibraryData({ name: '', address: '' });
                setShowAddForm(false);
                await fetchLibraries();
                setAlertType('success');
                setAlertMsg('Library added successfully.');
            } else {
                const body = await res.json();
                setAlertType('error');
                setAlertMsg(body.message || 'Failed to add library.');
            }
        } catch (err) {
            setAlertType('error');
            setAlertMsg('Error adding library.');
            console.error(err);
        }
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p style={{ color: 'red' }}>{error}</p>;

    return (
        <div className="handle-layout">
            <Navbar />
            <GlobalAlert message={alertMsg} type={alertType} onClose={() => setAlertMsg('')} />
            <div className="handle-container">
                {user && (
                    <div style={{ marginBottom: '1rem' }}>
                        <h2>Hello, {user.email}!</h2>
                        <p><strong>Email:</strong> {user.email}</p>
                        <p><strong>Role:</strong> {user.role}</p>
                    </div>
                )}

                <h3>Admin Dashboard</h3>

                <div className="button-group" style={{ flexWrap: 'wrap', marginBottom: '2rem' }}>
                    <Link to="/reservations"><button className="button">Manage All Reservations</button></Link>
                    <Link to="/loans"><button className="button">Manage All Loans</button></Link>
                    <Link to="/users"><button className="button">Manage Users</button></Link>
                </div>

                <h3>Libraries</h3>

                <input
                    type="search"
                    placeholder="Search library by name..."
                    value={searchLibrary}
                    onChange={e => setSearchLibrary(e.target.value)}
                    className="form-input"
                />

                <div style={{ marginBottom: '1.5rem' }}>
                    {!showAddForm ? (
                        <button className="button add-btn" onClick={() => setShowAddForm(true)}>Add Library</button>
                    ) : (
                        <div className="reservation-card" style={{ marginTop: '1rem' }}>
                            <p><strong>Name:</strong> <input value={newLibraryData.name} onChange={e => handleNewLibraryChange('name', e.target.value)} className="form-input" /></p>
                            <p><strong>Address:</strong> <input value={newLibraryData.address} onChange={e => handleNewLibraryChange('address', e.target.value)} className="form-input" /></p>
                            <div className="button-group">
                                <button className="button save-btn" onClick={handleAddLibrary}>Add</button>
                                <button className="button cancel-btn" onClick={() => { setShowAddForm(false); setNewLibraryData({ name: '', address: '' }); }}>Cancel</button>
                            </div>
                        </div>
                    )}
                </div>

                <div className="library-list-scroll" style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    {filteredLibraries.length === 0 ? (
                        <p>No libraries found.</p>
                    ) : (
                        filteredLibraries.map(lib => (
                            <div key={lib.id} className="reservation-card">
                                {editingLibraryId === lib.id ? (
                                    <>
                                        <p><strong>Name:</strong> <input value={editFormData.name} onChange={e => handleEditChange('name', e.target.value)} className="form-input" /></p>
                                        <p><strong>Address:</strong> <input value={editFormData.address} onChange={e => handleEditChange('address', e.target.value)} className="form-input" /></p>
                                        <div className="button-group">
                                            <button className="button save-btn" onClick={handleSaveEdit}>Save</button>
                                            <button className="button cancel-btn" onClick={() => setEditingLibraryId(null)}>Cancel</button>
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        <p><strong>{lib.name}</strong> (#{lib.id})</p>
                                        <p>Address: {lib.address}</p>
                                        <div className="button-group">
                                            <Link to={`/manage-resources/${lib.id}`}><button className="button res-btn">Manage Resources</button></Link>
                                            <Link to={`/users/library/${lib.id}`}><button className="button">View Staff</button></Link>
                                            <button className="button edit-btn" onClick={() => handleEdit(lib)}>Edit</button>
                                            <button className="button delete-btn" onClick={() => handleDelete(lib.id)}>Delete</button>
                                        </div>
                                    </>
                                )}
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default HomeAdminPage;
