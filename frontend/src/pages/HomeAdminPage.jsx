import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/HomeLibPage.css';
import {fetchWithAuth} from '../Api.js';


const HomeAdminPage = () => {
    const [libraries, setLibraries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchLibrary, setSearchLibrary] = useState('');
    const [editingLibraryId, setEditingLibraryId] = useState(null);
    const [editFormData, setEditFormData] = useState({name: '', address: ''});
    const [showAddForm, setShowAddForm] = useState(false);
    const [newLibraryData, setNewLibraryData] = useState({name: '', address: ''});
    const [user, setUser] = useState(null);


    const token = localStorage.getItem('accessToken');

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [librariesRes, userRes] = await Promise.all([
                    fetchWithAuth('/libraries'),
                    fetchWithAuth('/users/me')
                ]);

                if (!librariesRes.ok || !userRes.ok) throw new Error("Failed to fetch data.");

                const librariesData = await librariesRes.json();
                const userData = await userRes.json();

                setLibraries(librariesData);
                setUser(userData);
            } catch (err) {
                setError('Error loading data.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const filteredLibraries = libraries.filter(lib =>
        lib.name.toLowerCase().includes(searchLibrary.toLowerCase())
    );

    const handleEdit = (library) => {
        setEditingLibraryId(library.id);
        setEditFormData({name: library.name, address: library.address});
    };

    const handleEditChange = (field, value) => {
        setEditFormData(prev => ({...prev, [field]: value}));
    };

    const handleSaveEdit = async () => {
        try {
            const res = await fetchWithAuth(`/libraries/${editingLibraryId}`, {
                method: 'PUT',
                body: JSON.stringify(editFormData)
            });

            if (res.ok) {
                setEditingLibraryId(null);
                fetchLibraries();
            } else {
                alert('Failed to update library.');
            }
        } catch (err) {
            console.error(err);
            alert('Error updating library.');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this library?')) return;

        try {
            const res = await fetchWithAuth(`/libraries/${id}`, {
                method: 'DELETE'
            });

            if (res.ok) {
                setLibraries(libraries.filter(lib => lib.id !== id));
            } else {
                alert('Failed to delete library.');
            }
        } catch (err) {
            console.error(err);
            alert('Error deleting library.');
        }
    };

    const handleNewLibraryChange = (field, value) => {
        setNewLibraryData(prev => ({...prev, [field]: value}));
    };

    const handleAddLibrary = async () => {
        try {
            const res = await fetchWithAuth('/libraries', {
                method: 'POST',
                body: JSON.stringify(newLibraryData)
            });

            if (res.ok) {
                setNewLibraryData({name: '', address: ''});
                setShowAddForm(false);
                fetchLibraries();
            } else {
                alert('Failed to add library.');
            }
        } catch (err) {
            console.error(err);
            alert('Error adding library.');
        }
    };


    if (loading) return <p>Loading...</p>;
    if (error) return <p style={{color: 'red'}}>{error}</p>;

    return (
        <div className="handle-layout">
            <Navbar/>
            <div className="handle-container">

                {user && (
                    <div style={{marginBottom: '1rem'}}>
                        <h2>Hello, {user.email}!</h2>
                        <p><strong>Email:</strong> {user.email}</p>
                        <p><strong>Role:</strong> {user.role}</p>
                    </div>
                )}
                <br></br>


                <h3>Admin Dashboard</h3>

                <div className="button-group" style={{flexWrap: 'wrap', marginBottom: '2rem'}}>
                    <Link to="/reservations">
                        <button className="button">Manage All Reservations</button>
                    </Link>
                    <Link to="/loans">
                        <button className="button">Manage All Loans</button>
                    </Link>
                    <Link to="/users">
                        <button className="button">Manage Users</button>
                    </Link>
                </div>

                <h3>Libraries</h3>

                <input
                    type="search"
                    placeholder="Search library by name..."
                    value={searchLibrary}
                    onChange={e => setSearchLibrary(e.target.value)}
                    style={{
                        marginBottom: '1rem',
                        width: '100%',
                        padding: '0.5rem',
                        borderRadius: '6px',
                        border: '1px solid #4b5563',
                        backgroundColor: '#374151',
                        color: 'white',
                        fontSize: '1rem',
                    }}
                />

                <div style={{marginBottom: '1.5rem'}}>
                    {!showAddForm && (
                        <button className="button add-btn" onClick={() => setShowAddForm(true)}>
                            Add Library
                        </button>
                    )}

                    {showAddForm && (
                        <div className="reservation-card" style={{marginTop: '1rem'}}>
                            <p>
                                <strong>Name:</strong>{' '}
                                <input
                                    value={newLibraryData.name}
                                    onChange={e => handleNewLibraryChange('name', e.target.value)}
                                />
                            </p>
                            <p>
                                <strong>Address:</strong>{' '}
                                <input
                                    value={newLibraryData.address}
                                    onChange={e => handleNewLibraryChange('address', e.target.value)}
                                />
                            </p>
                            <div className="button-group">
                                <button className="button save-btn" onClick={handleAddLibrary}>Add</button>
                                <button className="button cancel-btn" onClick={() => {
                                    setShowAddForm(false);
                                    setNewLibraryData({name: '', address: ''});
                                }}>
                                    Cancel
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                <div className="library-list-scroll" style={{maxHeight: '300px', overflowY: 'auto'}}>
                    {filteredLibraries.length === 0 ? (
                        <p>No libraries found.</p>
                    ) : (
                        filteredLibraries.map(lib => (
                            <div key={lib.id} className="reservation-card">
                                {editingLibraryId === lib.id ? (
                                    <>
                                        <p>
                                            <strong>Name:</strong>{' '}
                                            <input
                                                value={editFormData.name}
                                                onChange={e => handleEditChange('name', e.target.value)}
                                            />
                                        </p>
                                        <p>
                                            <strong>Address:</strong>{' '}
                                            <input
                                                value={editFormData.address}
                                                onChange={e => handleEditChange('address', e.target.value)}
                                            />
                                        </p>
                                        <div className="button-group">
                                            <button className="button save-btn" onClick={handleSaveEdit}>Save</button>
                                            <button className="button cancel-btn"
                                                    onClick={() => setEditingLibraryId(null)}>Cancel
                                            </button>
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        <p><strong>{lib.name}</strong> (#{lib.id})</p>
                                        <p>Address: {lib.address}</p>
                                        <div className="button-group">
                                            <Link to={`/manage-resources/${lib.id}`}>
                                                <button className="button res-btn">Manage Resources</button>
                                            </Link>
                                            <Link to={`/users/library/${lib.id}`}>
                                                <button className="button">View Staff</button>
                                            </Link>
                                            <button className="button edit-btn" onClick={() => handleEdit(lib)}>Edit
                                            </button>
                                            <button className="button delete-btn"
                                                    onClick={() => handleDelete(lib.id)}>Delete
                                            </button>
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
