import React, {useEffect, useState} from 'react';
import Select from 'react-select';
import Navbar from '../components/Navbar';
import '../styles/ManageResources.css';
import '../styles/ReservationPage.css';
import {fetchWithAuth} from '../Api.js';

const LoanPage = () => {
    const [loans, setLoans] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [searchTitle, setSearchTitle] = useState('');
    const [searchEmail, setSearchEmail] = useState('');
    const [userRole, setUserRole] = useState(null);
    const [books, setBooks] = useState([]);
    const [users, setUsers] = useState([]);
    const [filterStatus, setFilterStatus] = useState('All');
    const [libraries, setLibraries] = useState([]);
    const [selectedLibrary, setSelectedLibrary] = useState('All');
    const [manualLoanLibrary, setManualLoanLibrary] = useState('All');
    const [availableCopies, setAvailableCopies] = useState([]);
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [selectedUserEmail, setSelectedUserEmail] = useState(null);
    const [selectedCopyId, setSelectedCopyId] = useState(null);

    const token = localStorage.getItem('accessToken');

    useEffect(() => {
        const fetchUserAndLibraries = async () => {
            try {
                const userRes = await fetchWithAuth(`/users/me`);
                const userData = await userRes.json();
                setUserRole(userData.role);

                if (userData.role === 'ADMIN') {
                    const libsRes = await fetchWithAuth(`/libraries`);
                    if (libsRes.ok) {
                        const libsData = await libsRes.json();
                        const libsWithAll = [{ id: 'All', name: 'All Libraries' }, ...libsData];
                        const sortedLibs = [libsWithAll[0], ...libsWithAll.slice(1).sort((a,b) => a.name.localeCompare(b.name))];
                        setLibraries(sortedLibs);
                        setSelectedLibrary('All');
                        setManualLoanLibrary('All');
                    }
                } else {
                    setSelectedLibrary(userData.libraryId);
                    setManualLoanLibrary(userData.libraryId);
                }
            } catch (err) {
                console.error('Error fetching user and libraries:', err);
            }
        };
        fetchUserAndLibraries();
    }, []);

    useEffect(() => {
        if (!userRole || !selectedLibrary) return;

        const fetchData = async () => {
            try {
                let loansUrl = '';
                if (userRole === 'ADMIN') {
                    loansUrl = selectedLibrary === 'All' ? `/loans` : `/loans/library/${selectedLibrary}`;
                } else {
                    loansUrl = `/loans/library/${selectedLibrary}`;
                }

                const loansRes = await fetchWithAuth(loansUrl);
                if (loansRes.ok) {
                    const loansData = await loansRes.json();
                    setLoans(loansData.sort((a,b) => new Date(b.startDate) - new Date(a.startDate)));
                }

                const booksRes = await fetchWithAuth(`/books`);
                if (booksRes.ok) {
                    const booksData = await booksRes.json();
                    setBooks(booksData);
                }

                const usersRes = await fetchWithAuth(`/users/search?role=USER`);
                if (usersRes.ok) {
                    const usersData = await usersRes.json();
                    setUsers(usersData);
                }
            } catch (err) {
                console.error('Error loading data:', err);
            }
        };

        fetchData();
    }, [selectedLibrary, userRole]);

    useEffect(() => {
        const fetchAvailableCopies = async () => {
            try {
                let url = '';
                if (manualLoanLibrary === 'All' || !manualLoanLibrary) {
                    url = `/copies/available`;
                } else {
                    url = `/copies/library/${manualLoanLibrary}/available`;
                }
                const res = await fetchWithAuth(url);
                if (!res.ok) throw new Error('Failed to fetch available copies');
                const data = await res.json();
                setAvailableCopies(data);
                setSelectedCopyId(null);
            } catch (err) {
                console.error(err);
                setAvailableCopies([]);
                setSelectedCopyId(null);
            }
        };

        fetchAvailableCopies();
    }, [manualLoanLibrary]);

    useEffect(() => {
        let result = [...loans];

        console.log('Filtering loans with selectedUserId:', selectedUserId);
        console.log('Loans:', loans);

        if (searchTitle.trim()) {
            result = result.filter(l => (l.title || '').toLowerCase().includes(searchTitle.toLowerCase()));
        }

        if (selectedUserId) {
            result = result.filter(l => l.userID === selectedUserId);
        }

        if (filterStatus === 'Active') {
            result = result.filter(l => l.returnDate === null);
        } else if (filterStatus === 'Returned') {
            result = result.filter(l => l.returnDate !== null);
        }

        setFiltered(result);
    }, [searchTitle, selectedUserId, loans, filterStatus]);

    const returnLoan = async (id) => {
        try {
            const res = await fetchWithAuth(`/loans/${id}/return`, {
                method: 'POST'
            });

            if (res.ok) {
                const updatedLoans = await fetchWithAuth(selectedLibrary === 'All' ? '/loans' : `/loans/library/${selectedLibrary}`);
                if (updatedLoans.ok) {
                    const data = await updatedLoans.json();
                    setLoans(data.sort((a, b) => new Date(b.startDate) - new Date(a.startDate)));
                }
            }
        } catch (err) {
            console.error('Error returning loan:', err);
        }
    };

    const handleManualLoan = async () => {
        if (!selectedUserId || !selectedCopyId) {
            alert('Please select both a user and a copy.');
            return;
        }

        try {
            const res = await fetchWithAuth(`/loans?userId=${selectedUserId}&copyId=${selectedCopyId}`, {
                method: 'POST'
            });

            if (!res.ok) throw new Error("Loan creation failed");

            alert('Loan created successfully!');
            setSelectedUserId(null);
            setSelectedCopyId(null);

            const loansRes = await fetchWithAuth(selectedLibrary === 'All' ? '/loans' : `/loans/library/${selectedLibrary}`);
            if (loansRes.ok) {
                const loansData = await loansRes.json();
                setLoans(loansData.sort((a, b) => new Date(b.startDate) - new Date(a.startDate)));
            }

        } catch (err) {
            alert('Error creating loan.');
            console.error(err);
        }
    };

    const bookOptions = [...books]
        .sort((a, b) => a.title.localeCompare(b.title))
        .map(b => ({
            value: b.title,
            label: b.title
        }));

    const userOptions = [...users]
        .sort((a, b) => a.email.localeCompare(b.email))
        .map(u => ({
            value: u.id,
            label: u.email
        }));


    const statusOptions = [
        {value: 'All', label: 'All'},
        {value: 'Active', label: 'Active'},
        {value: 'Returned', label: 'Returned'}
    ];
    const libraryOptions = libraries.map(lib => ({
        value: lib.id,
        label: lib.name
    }));

    const copyOptions = availableCopies.map(copy => ({
        value: copy.id,
        label: `${copy.book.title} (Copy #${copy.id})`
    }));

    const customSelectStyles = {
        option: (base, state) => ({
            ...base,
            backgroundColor: state.isSelected
                ? '#4b5563'
                : state.isFocused
                    ? '#374151'
                    : '#1f2937',
            color: 'white',
            cursor: 'pointer',
        }),
        control: (base, state) => ({
            ...base,
            backgroundColor: '#374151',
            borderColor: '#4b5563',
            boxShadow: state.isFocused ? '0 0 0 1px #6b7280' : 'none',
            '&:hover': {
                borderColor: '#6b7280',
            },
        }),
        menu: (base) => ({
            ...base,
            backgroundColor: '#1f2937',
            color: 'white',
        }),
        singleValue: (base) => ({
            ...base,
            color: 'white',
        }),
        placeholder: (base) => ({
            ...base,
            color: '#9ca3af',
        }),
        input: (base) => ({
            ...base,
            color: 'white',
        }),
    };

    return (
        <div className="manage-layout">
            <Navbar/>
            <div className="resource-container">
                {userRole === 'ADMIN' && (
                    <div className="manual-loan-section" style={{marginTop: '2rem', padding: '1rem', border: '1px solid #444', borderRadius: '6px'}}>
                        <h3>Manual Loan Creation</h3>

                        <div className="filter-container" style={{marginBottom: '1rem'}}>
                            <label>Select Library:</label>
                            <Select
                                options={libraryOptions}
                                value={libraryOptions.find(opt => opt.value === manualLoanLibrary) || null}
                                onChange={option => setManualLoanLibrary(option ? option.value : 'All')}
                                styles={customSelectStyles}
                                isClearable={false}
                            />
                        </div>

                        <div className="filter-container" style={{marginBottom: '1rem'}}>
                            <label>Select User:</label>
                            <Select
                                options={userOptions}
                                value={userOptions.find(opt => opt.value === selectedUserId) || null}
                                onChange={option => {
                                    setSelectedUserId(option ? option.value : null);
                                    setSelectedUserEmail(option ? option.label : null);
                                }}
                                styles={customSelectStyles}
                                isClearable
                                placeholder="Search user by email"
                            />
                        </div>

                        <div className="filter-container" style={{marginBottom: '1rem'}}>
                            <label>Select Available Copy:</label>
                            <Select
                                options={copyOptions}
                                value={
                                    selectedCopyId
                                        ? copyOptions.find(c => c.value === selectedCopyId) || null
                                        : null
                                }
                                onChange={option => setSelectedCopyId(option ? option.value : null)}
                                styles={customSelectStyles}
                                isClearable
                                placeholder="Select a copy"
                                noOptionsMessage={() => 'No copies available'}
                            />
                        </div>

                        <button
                            className="create-btn"
                            onClick={handleManualLoan}
                            disabled={!selectedUserEmail || !selectedCopyId}
                        >
                            Create Loan
                        </button>
                    </div>
                )}

                <br></br>
                <br></br>
                <h3>Loans</h3>

                <div className="search-section">

                    <div className="filter-container">
                        <label>Filter by Status:</label>
                        <Select
                            options={statusOptions}
                            value={statusOptions.find(opt => opt.value === filterStatus)}
                            onChange={selected => setFilterStatus(selected ? selected.value : 'All')}
                            styles={customSelectStyles}
                            isClearable={false}
                        />
                    </div>

                    <div className="filter-container">
                        <label>Search by Title:</label>
                        <Select
                            options={bookOptions}
                            value={bookOptions.find(b => b.value === searchTitle) || null}
                            onChange={selected => setSearchTitle(selected ? selected.value : '')}
                            styles={customSelectStyles}
                            isClearable
                            placeholder="Select title"
                        />
                    </div>

                    <div className="filter-container">
                        <label>Search by User Email:</label>
                        <Select
                            options={userOptions}
                            value={userOptions.find(opt => opt.value === selectedUserId) || null}
                            onChange={option => {
                                if (option) {
                                    setSelectedUserId(option.value);
                                    const user = users.find(u => u.id === option.value);
                                    setSelectedUserEmail(user ? user.email : null);
                                } else {
                                    setSelectedUserId(null);
                                    setSelectedUserEmail(null);
                                }
                            }}
                            styles={customSelectStyles}
                            isClearable
                            placeholder="Search user by email"
                        />
                    </div>

                </div>

                <div className="book-list-section">
                    {filtered.length === 0 ? (
                        <p>No loans found.</p>
                    ) : (
                        <div className="book-list-grid">
                            {filtered.map(loan => {
                                const isActive = loan.returnDate === null;
                                return (
                                    <div key={loan.id} className="book-holder-wrapper">
                                        <div className="book-holder">
                                            <div className="book-header" style={{cursor: 'default'}}>
                                                <div className="book-info">
                                                    <h4>Loan #{loan.id}</h4>
                                                    <p><strong>User:</strong> {loan.email}</p>
                                                    <p><strong>Copy:</strong> {loan.title} (#{loan.copyId})</p>
                                                    <p>
                                                        <strong>Status:</strong>{' '}
                                                        <span
                                                            className={`status-label ${isActive ? 'status-active' : 'status-returned'}`}>{isActive ? 'Active' : 'Returned'}</span>
                                                    </p>
                                                    <p>
                                                        <strong>Start:</strong> {new Date(loan.startDate).toLocaleDateString()}
                                                    </p>
                                                    <p><strong>Due
                                                        Date:</strong> {new Date(loan.endDate).toLocaleDateString()}</p>
                                                    {loan.returnDate && (
                                                        <p>
                                                            <strong>Returned:</strong> {new Date(loan.returnDate).toLocaleDateString()}
                                                        </p>
                                                    )}
                                                </div>

                                                {isActive && (
                                                    <div className="book-buttons">
                                                        <button className="delete-btn"
                                                                onClick={() => returnLoan(loan.id)}>Return
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LoanPage;